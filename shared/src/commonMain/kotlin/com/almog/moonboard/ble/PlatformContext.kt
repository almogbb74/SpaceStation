package com.almog.moonboard.ble

// Android needs a Context to get the BluetoothManager; iOS needs nothing.
// This expect/actual lets MoonBoardBleClient's constructor look the same from common code.
expect class PlatformContext
