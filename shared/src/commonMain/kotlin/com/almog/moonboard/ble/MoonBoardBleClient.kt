package com.almog.moonboard.ble

import com.almog.moonboard.model.PlacedHold
import kotlinx.coroutines.flow.StateFlow

/** Platform BLE backend: Android's BluetoothGatt / iOS's CoreBluetooth. */
expect class MoonBoardBleClient(context: PlatformContext) {
    val connectionState: StateFlow<ConnectionState>
    val scanResults: StateFlow<List<BleDevice>>

    fun startScan()
    fun stopScan()
    fun connect(device: BleDevice)
    fun disconnect()
    suspend fun sendProblem(holds: List<PlacedHold>)
    fun close()
}
