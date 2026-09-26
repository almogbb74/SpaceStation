@file:Suppress("FunctionName") // Composable functions should start with an uppercase, so I suppressed the warning for now.

package com.almog.moonboard.android

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.BluetoothSearching
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.almog.moonboard.android.ui.theme.MoonBoardBackground
import com.almog.moonboard.android.ui.theme.MoonBoardSuccess
import com.almog.moonboard.android.ui.theme.MoonBoardTextMuted
import com.almog.moonboard.ble.BleDevice
import com.almog.moonboard.ble.ConnectionState

// Icon (22dp) and spinner (16dp) are different sizes, so each gets a slot of this width -
// keeps the title and status text starting at the same left edge regardless of glyph size.
private val LeadingColumnWidth = 22.dp

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
        containerColor = MoonBoardBackground,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.width(LeadingColumnWidth), contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Bluetooth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(stringResource(R.string.connect_dialog_title), fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                when (connectionState) {
                    is ConnectionState.Connected ->
                        StatusRow(
                            icon = Icons.Default.CheckCircle,
                            tint = MoonBoardSuccess,
                            text = stringResource(R.string.status_connected, connectionState.deviceName),
                        )

                    is ConnectionState.Connecting ->
                        StatusRow(
                            spinner = true,
                            tint = MoonBoardTextMuted,
                            text = stringResource(R.string.status_connecting, connectionState.deviceName),
                        )

                    is ConnectionState.Error ->
                        StatusRow(
                            icon = Icons.Default.Error,
                            tint = MaterialTheme.colorScheme.error,
                            text = stringResource(R.string.status_error, connectionState.message),
                        )

                    is ConnectionState.BluetoothOff ->
                        StatusRow(
                            icon = Icons.Default.BluetoothDisabled,
                            tint = MaterialTheme.colorScheme.error,
                            text = stringResource(R.string.status_bluetooth_off),
                        )

                    else -> {
                        StatusRow(spinner = true, tint = MoonBoardTextMuted, text = stringResource(R.string.status_scanning))
                        if (devices.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                                Column {
                                    devices.forEachIndexed { index, device ->
                                        if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.background)
                                        DeviceRow(device, onClick = { onDeviceSelected(device) })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (connectionState is ConnectionState.Connected) {
                TextButton(onClick = { onDisconnect(); onDismiss() }) { Text(stringResource(R.string.action_disconnect)) }
            } else {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_close)) }
            }
        }
    )
}

@Composable
private fun StatusRow(
    icon: ImageVector? = null,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    text: String,
    spinner: Boolean = false,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.width(LeadingColumnWidth), contentAlignment = Alignment.Center) {
            when {
                spinner -> CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                icon != null -> Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(Modifier.width(10.dp))
        Text(text, color = tint, fontSize = 14.sp)
    }
}

@Composable
private fun DeviceRow(device: BleDevice, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.AutoMirrored.Filled.BluetoothSearching, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Text(device.name, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
