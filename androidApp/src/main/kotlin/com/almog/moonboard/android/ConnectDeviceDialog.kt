package com.almog.moonboard.android

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.almog.moonboard.ble.BleDevice
import com.almog.moonboard.ble.ConnectionState

@Composable
fun ConnectDeviceDialog(
    connectionState: ConnectionState,
    devices: List<BleDevice>,
    onDeviceSelected: (BleDevice) -> Unit,
    onDisconnect: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("MoonBoard") },
        text = {
            Column {
                when (connectionState) {
                    is ConnectionState.Connected -> Text("Connected to ${connectionState.deviceName}")
                    is ConnectionState.Connecting -> Text("Connecting to ${connectionState.deviceName}...")
                    else -> {
                        if (connectionState is ConnectionState.Error) {
                            Text("Error: ${connectionState.message}")
                        } else {
                            Text("Scanning for nearby MoonBoards...")
                        }
                        if (devices.isEmpty()) {
                            Text("No MoonBoard found yet")
                        } else {
                            devices.forEach { device ->
                                Text(
                                    device.name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onDeviceSelected(device) }
                                        .padding(vertical = 12.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (connectionState is ConnectionState.Connected) {
                TextButton(onClick = { onDisconnect(); onDismiss() }) { Text("Disconnect") }
            } else {
                TextButton(onClick = onDismiss) { Text("Close") }
            }
        }
    )
}
