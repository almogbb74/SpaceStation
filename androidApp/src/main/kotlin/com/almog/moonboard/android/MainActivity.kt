package com.almog.moonboard.android

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.almog.moonboard.android.ui.theme.MoonBoardTheme
import com.almog.moonboard.ble.MoonBoardBleClient
import com.almog.moonboard.ble.PlatformContext
import com.almog.moonboard.viewmodel.BoardViewModel
import com.almog.moonboard.viewmodel.SnakeViewModel

class MainActivity : ComponentActivity() {

    private val bleClient by lazy { MoonBoardBleClient(PlatformContext(applicationContext)) }
    private val boardViewModel by lazy { BoardViewModel(bleClient) }
    private val snakeViewModel by lazy { SnakeViewModel(bleClient) }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestBlePermissions()
        setContent { MoonBoardTheme { MoonBoardApp(boardViewModel, snakeViewModel) } }
    }

    private fun requestBlePermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        permissionLauncher.launch(permissions)
    }

    override fun onDestroy() {
        super.onDestroy()
        boardViewModel.onCleared()
        snakeViewModel.onCleared()
        bleClient.close()
    }
}
