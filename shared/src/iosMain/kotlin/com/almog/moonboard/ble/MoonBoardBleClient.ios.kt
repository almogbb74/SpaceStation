package com.almog.moonboard.ble

import com.almog.moonboard.model.PlacedHold
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.CoreBluetooth.CBCentralManager
import platform.CoreBluetooth.CBCentralManagerDelegateProtocol
import platform.CoreBluetooth.CBCharacteristic
import platform.CoreBluetooth.CBCharacteristicWriteWithoutResponse
import platform.CoreBluetooth.CBPeripheral
import platform.CoreBluetooth.CBPeripheralDelegateProtocol
import platform.CoreBluetooth.CBService
import platform.CoreBluetooth.CBUUID
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSNumber
import platform.Foundation.create
import platform.darwin.NSObject

// NOTE: written against the documented CoreBluetooth Kotlin/Native API surface but never
// compiled - there is no Mac/Xcode available in this environment. Expect to fix small
// interop naming mismatches (delegate method signatures, object/data object singleton
// access) the first time this is built in Xcode.
actual class MoonBoardBleClient actual constructor(context: PlatformContext) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    actual val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _scanResults = MutableStateFlow<List<BleDevice>>(emptyList())
    actual val scanResults: StateFlow<List<BleDevice>> = _scanResults.asStateFlow()

    private val serviceUuid = CBUUID.UUIDWithString(MoonBoardProtocol.SERVICE_UUID)
    private val writeCharUuid = CBUUID.UUIDWithString(MoonBoardProtocol.WRITE_CHARACTERISTIC_UUID)

    private val foundPeripherals = mutableMapOf<String, CBPeripheral>()
    private var connectedPeripheral: CBPeripheral? = null
    private var writeCharacteristic: CBCharacteristic? = null

    private val centralDelegate = object : NSObject(), CBCentralManagerDelegateProtocol {
        override fun centralManagerDidUpdateState(central: CBCentralManager) {
            // Scan/connect calls are only made after the user taps "scan", by which point
            // the app has been open long enough for the central to be powered on.
        }

        override fun centralManager(
            central: CBCentralManager,
            didDiscoverPeripheral: CBPeripheral,
            advertisementData: Map<Any?, *>,
            RSSI: NSNumber
        ) {
            val name = didDiscoverPeripheral.name ?: return
            if (!name.startsWith(MoonBoardProtocol.DEVICE_NAME_PREFIX, ignoreCase = true)) return
            val id = didDiscoverPeripheral.identifier.UUIDString
            foundPeripherals[id] = didDiscoverPeripheral
            _scanResults.value = foundPeripherals.values.map { BleDevice(it.identifier.UUIDString, it.name ?: id) }
        }

        override fun centralManager(central: CBCentralManager, didConnectPeripheral: CBPeripheral) {
            didConnectPeripheral.delegate = peripheralDelegate
            didConnectPeripheral.discoverServices(listOf(serviceUuid))
        }

        override fun centralManager(
            central: CBCentralManager,
            didFailToConnectPeripheral: CBPeripheral,
            error: NSError?
        ) {
            _connectionState.value = ConnectionState.Error("Connect failed: ${error?.localizedDescription}")
        }

        override fun centralManager(
            central: CBCentralManager,
            didDisconnectPeripheral: CBPeripheral,
            error: NSError?
        ) {
            _connectionState.value = ConnectionState.Disconnected
            writeCharacteristic = null
            connectedPeripheral = null
        }
    }

    private val peripheralDelegate = object : NSObject(), CBPeripheralDelegateProtocol {
        override fun peripheral(peripheral: CBPeripheral, didDiscoverServices: NSError?) {
            val service = peripheral.services?.filterIsInstance<CBService>()?.firstOrNull { it.UUID.isEqual(serviceUuid) }
            if (service == null) {
                _connectionState.value = ConnectionState.Error("MoonBoard UART service not found")
                return
            }
            peripheral.discoverCharacteristics(listOf(writeCharUuid), service)
        }

        override fun peripheral(
            peripheral: CBPeripheral,
            didDiscoverCharacteristicsForService: CBService,
            error: NSError?
        ) {
            val characteristic = didDiscoverCharacteristicsForService.characteristics
                ?.filterIsInstance<CBCharacteristic>()
                ?.firstOrNull { it.UUID.isEqual(writeCharUuid) }
            if (characteristic == null) {
                _connectionState.value = ConnectionState.Error("MoonBoard write characteristic not found")
                return
            }
            writeCharacteristic = characteristic
            _connectionState.value = ConnectionState.Connected(peripheral.name ?: peripheral.identifier.UUIDString)
        }
    }

    private val centralManager = CBCentralManager(delegate = centralDelegate, queue = null)

    actual fun startScan() {
        foundPeripherals.clear()
        _scanResults.value = emptyList()
        _connectionState.value = ConnectionState.Scanning
        centralManager.scanForPeripheralsWithServices(listOf(serviceUuid), options = null)
    }

    actual fun stopScan() {
        centralManager.stopScan()
        if (_connectionState.value is ConnectionState.Scanning) {
            _connectionState.value = ConnectionState.Disconnected
        }
    }

    actual fun connect(device: BleDevice) {
        stopScan()
        val peripheral = foundPeripherals[device.id] ?: return
        _connectionState.value = ConnectionState.Connecting(device.name)
        connectedPeripheral = peripheral
        centralManager.connectPeripheral(peripheral, options = null)
    }

    actual fun disconnect() {
        connectedPeripheral?.let { centralManager.cancelPeripheralConnection(it) }
    }

    actual suspend fun sendProblem(holds: List<PlacedHold>) {
        val peripheral = connectedPeripheral ?: return
        val characteristic = writeCharacteristic ?: return
        val payload = PayloadBuilder.build(holds)
        for (chunk in MoonBoardProtocol.chunk(payload)) {
            writeChunk(peripheral, characteristic, chunk)
            delay(MoonBoardProtocol.CHUNK_DELAY_MS)
        }
    }

    private fun writeChunk(peripheral: CBPeripheral, characteristic: CBCharacteristic, chunk: ByteArray) {
        val data = chunk.usePinned { pinned -> NSData.create(bytes = pinned.addressOf(0), length = chunk.size.toULong()) }
        peripheral.writeValue(data, characteristic, CBCharacteristicWriteWithoutResponse)
    }

    actual fun close() {
        connectedPeripheral?.let { centralManager.cancelPeripheralConnection(it) }
        scope.cancel()
    }
}
