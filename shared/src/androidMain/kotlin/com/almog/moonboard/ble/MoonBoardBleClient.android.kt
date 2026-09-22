package com.almog.moonboard.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.Build
import android.os.ParcelUuid
import com.almog.moonboard.debug.AppLogger
import com.almog.moonboard.model.PlacedHold
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

private const val TAG = "BLE"

actual class MoonBoardBleClient actual constructor(context: PlatformContext) {

    init {
        AppLogger.init(context)
        AppLogger.log(TAG, "MoonBoardBleClient initialized")
    }

    private val androidContext = context.context
    private val bluetoothManager =
        androidContext.getSystemService(android.content.Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter get() = bluetoothManager.adapter
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    actual val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _scanResults = MutableStateFlow<List<BleDevice>>(emptyList())
    actual val scanResults: StateFlow<List<BleDevice>> = _scanResults.asStateFlow()

    private var gatt: BluetoothGatt? = null
    private var writeCharacteristic: BluetoothGattCharacteristic? = null
    private val foundDevices = mutableMapOf<String, BluetoothDevice>()

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val name = device.name ?: return
            if (!name.startsWith(MoonBoardProtocol.DEVICE_NAME_PREFIX, ignoreCase = true)) return
            if (foundDevices.put(device.address, device) == null) {
                AppLogger.log(TAG, "Scan found device: $name (${device.address}), rssi=${result.rssi}")
            }
            _scanResults.value = foundDevices.values.map { BleDevice(it.address, it.name ?: it.address) }
        }

        override fun onScanFailed(errorCode: Int) {
            AppLogger.log(TAG, "Scan failed: errorCode=$errorCode")
            _connectionState.value = ConnectionState.Error("Scan failed: $errorCode")
        }
    }

    @SuppressLint("MissingPermission")
    actual fun startScan() {
        AppLogger.log(TAG, "startScan() - adapter enabled=${adapter?.isEnabled}")
        foundDevices.clear()
        _scanResults.value = emptyList()
        _connectionState.value = ConnectionState.Scanning
        val filters = listOf(
            ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(UUID.fromString(MoonBoardProtocol.SERVICE_UUID)))
                .build()
        )
        val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        adapter.bluetoothLeScanner?.startScan(filters, settings, scanCallback)
    }

    @SuppressLint("MissingPermission")
    actual fun stopScan() {
        AppLogger.log(TAG, "stopScan(), found ${foundDevices.size} device(s) this session")
        adapter.bluetoothLeScanner?.stopScan(scanCallback)
        if (_connectionState.value is ConnectionState.Scanning) {
            _connectionState.value = ConnectionState.Disconnected
        }
    }

    @SuppressLint("MissingPermission")
    actual fun connect(device: BleDevice) {
        AppLogger.log(TAG, "connect() to ${device.name} (${device.id})")
        stopScan()
        val btDevice = foundDevices[device.id] ?: adapter.getRemoteDevice(device.id)
        _connectionState.value = ConnectionState.Connecting(device.name)
        gatt = btDevice.connectGatt(androidContext, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
    }

    @SuppressLint("MissingPermission")
    actual fun disconnect() {
        AppLogger.log(TAG, "disconnect() requested")
        gatt?.disconnect()
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(g: BluetoothGatt, status: Int, newState: Int) {
            AppLogger.log(TAG, "onConnectionStateChange: status=$status newState=$newState")
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> g.discoverServices()
                BluetoothProfile.STATE_DISCONNECTED -> {
                    _connectionState.value = ConnectionState.Disconnected
                    writeCharacteristic = null
                    g.close()
                    if (gatt === g) gatt = null
                }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
            AppLogger.log(TAG, "onServicesDiscovered: status=$status")
            if (status != BluetoothGatt.GATT_SUCCESS) {
                _connectionState.value = ConnectionState.Error("Service discovery failed: $status")
                return
            }
            AppLogger.log(TAG, "Discovered services: ${g.services.map { it.uuid }}")
            val service = g.getService(UUID.fromString(MoonBoardProtocol.SERVICE_UUID))
            writeCharacteristic = service?.getCharacteristic(UUID.fromString(MoonBoardProtocol.WRITE_CHARACTERISTIC_UUID))
            if (writeCharacteristic == null) {
                AppLogger.log(TAG, "MoonBoard UART service/characteristic NOT found on this device")
                _connectionState.value = ConnectionState.Error("MoonBoard UART characteristic not found")
                return
            }
            AppLogger.log(TAG, "Connected: ${g.device.name ?: g.device.address}")
            _connectionState.value = ConnectionState.Connected(g.device.name ?: g.device.address)
        }
    }

    actual suspend fun sendProblem(holds: List<PlacedHold>) {
        val g = gatt
        val characteristic = writeCharacteristic
        if (g == null || characteristic == null) {
            AppLogger.log(TAG, "sendProblem() called with no active connection - ignoring")
            return
        }
        val payload = PayloadBuilder.build(holds)
        val chunks = MoonBoardProtocol.chunk(payload)
        AppLogger.log(TAG, "sendProblem: payload=\"$payload\" (${chunks.size} chunk(s))")
        for ((index, chunk) in chunks.withIndex()) {
            val ok = writeChunk(g, characteristic, chunk)
            AppLogger.log(TAG, "  chunk ${index + 1}/${chunks.size}: ${chunk.size}B, writeCharacteristic returned $ok")
            delay(MoonBoardProtocol.CHUNK_DELAY_MS)
        }
    }

    @SuppressLint("MissingPermission")
    private fun writeChunk(g: BluetoothGatt, characteristic: BluetoothGattCharacteristic, chunk: ByteArray): Any {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            g.writeCharacteristic(characteristic, chunk, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE)
        } else {
            @Suppress("DEPRECATION")
            characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            @Suppress("DEPRECATION")
            characteristic.value = chunk
            @Suppress("DEPRECATION")
            g.writeCharacteristic(characteristic)
        }
    }

    @SuppressLint("MissingPermission")
    actual fun close() {
        AppLogger.log(TAG, "close()")
        gatt?.close()
        gatt = null
        scope.cancel()
    }
}
