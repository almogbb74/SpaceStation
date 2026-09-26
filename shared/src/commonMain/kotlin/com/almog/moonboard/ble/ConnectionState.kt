package com.almog.moonboard.ble

sealed class ConnectionState {
    data object Disconnected : ConnectionState()
    data object BluetoothOff : ConnectionState()
    data object Scanning : ConnectionState()
    data class Connecting(val deviceName: String) : ConnectionState()
    data class Connected(val deviceName: String) : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}

data class BleDevice(val id: String, val name: String)
