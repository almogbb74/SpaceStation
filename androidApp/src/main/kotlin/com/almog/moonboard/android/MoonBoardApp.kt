@file:Suppress("FunctionName") // Composable functions should start with an uppercase, so I suppressed the warning for now.

package com.almog.moonboard.android

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.almog.moonboard.android.ui.theme.IbmPlexMonoMedium
import com.almog.moonboard.android.ui.theme.MoonBoardAccent
import com.almog.moonboard.android.ui.theme.MoonBoardTextMuted
import com.almog.moonboard.ble.ConnectionState
import com.almog.moonboard.viewmodel.BoardViewModel
import com.almog.moonboard.viewmodel.SnakeViewModel
import kotlinx.coroutines.launch

private data class DrawerDestination(val route: String, @get:StringRes val label: Int, val icon: ImageVector)

// Grid is the start destination. Add one entry here + one `composable(...)` below per future
// game screen (Pong, ...) - the drawer and NavHost don't need any other changes.
private val drawerDestinations = listOf(
    DrawerDestination("grid", R.string.nav_grid, Icons.Default.GridOn),
    DrawerDestination("snake", R.string.nav_snake, Icons.Default.SportsEsports),
    DrawerDestination("settings", R.string.nav_settings, Icons.Default.Settings),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoonBoardApp(boardViewModel: BoardViewModel, snakeViewModel: SnakeViewModel) {
    val state by boardViewModel.uiState.collectAsState()
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                drawerDestinations.forEach { destination ->
                    NavigationDrawerItem(
                        label = { Text(stringResource(destination.label)) },
                        icon = { Icon(destination.icon, contentDescription = null) },
                        selected = currentRoute == destination.route,
                        onClick = {
                            navController.navigate(destination.route) { launchSingleTop = true }
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                Column {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                stringResource(R.string.app_name).uppercase(),
                                fontFamily = IbmPlexMonoMedium,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                                letterSpacing = 1.5.sp,
                                color = MoonBoardTextMuted,
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.content_desc_menu))
                            }
                        },
                        actions = {
                            IconButton(onClick = boardViewModel::openConnectDialog) {
                                val connected = state.connectionState is ConnectionState.Connected
                                Icon(
                                    if (connected) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
                                    contentDescription = stringResource(R.string.content_desc_connect)
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                    )
                    Box(Modifier.fillMaxWidth().height(3.dp).background(MoonBoardAccent))
                }
            }
        ) { padding ->
            NavHost(navController, startDestination = "grid", modifier = Modifier.padding(padding)) {
                composable("grid") { GridScreen(boardViewModel) }
                composable("snake") { SnakeScreen(snakeViewModel) }
                composable("settings") { SettingsScreen(boardViewModel) }
            }
        }
    }

    if (state.isConnectDialogOpen) {
        ConnectDeviceDialog(
            connectionState = state.connectionState,
            devices = state.availableDevices,
            onDeviceSelected = boardViewModel::connect,
            onDisconnect = boardViewModel::disconnect,
            onDismiss = boardViewModel::closeConnectDialog,
        )
    }
}
