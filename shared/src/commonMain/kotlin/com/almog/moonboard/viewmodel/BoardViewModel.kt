package com.almog.moonboard.viewmodel

import com.almog.moonboard.ble.BleDevice
import com.almog.moonboard.ble.ConnectionState
import com.almog.moonboard.ble.MoonBoardBleClient
import com.almog.moonboard.debug.AppLogger
import com.almog.moonboard.model.BoardSetup
import com.almog.moonboard.model.GridPosition
import com.almog.moonboard.model.HoldType
import com.almog.moonboard.model.PlacedHold
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BoardUiState(
    val connectionState: ConnectionState = ConnectionState.Disconnected,
    val availableDevices: List<BleDevice> = emptyList(),
    val selectedSetup: BoardSetup = BoardSetup.MOONBOARD_2024,
    val holds: Map<GridPosition, HoldType> = emptyMap(),
    val isConnectDialogOpen: Boolean = false,
)

/**
 * No database: the "model" here is just in-memory UI state plus the BLE client, which is
 * exactly what MVVM expects the Model layer to be when there's nothing to persist -
 * Repository/Model just wraps hardware I/O instead of disk I/O.
 */
class BoardViewModel(private val bleClient: MoonBoardBleClient) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _uiState = MutableStateFlow(BoardUiState())
    val uiState: StateFlow<BoardUiState> = _uiState.asStateFlow()

    init {
        scope.launch {
            bleClient.connectionState.collect { state -> _uiState.update { it.copy(connectionState = state) } }
        }
        scope.launch {
            bleClient.scanResults.collect { devices -> _uiState.update { it.copy(availableDevices = devices) } }
        }
    }

    /** Opens the connect modal and starts scanning; the View just renders isConnectDialogOpen. */
    fun openConnectDialog() {
        _uiState.update { it.copy(isConnectDialogOpen = true) }
        bleClient.startScan()
    }

    fun closeConnectDialog() {
        _uiState.update { it.copy(isConnectDialogOpen = false) }
        bleClient.stopScan()
    }

    fun connect(device: BleDevice) {
        AppLogger.log("ViewModel", "User connecting to ${device.name}")
        bleClient.connect(device)
        _uiState.update { it.copy(isConnectDialogOpen = false) }
    }

    fun disconnect() = bleClient.disconnect()

    fun selectSetup(setup: BoardSetup) {
        AppLogger.log("ViewModel", "Setup changed to ${setup.displayName}")
        _uiState.update { state ->
            val occupied = setup.occupiedPositions()
            state.copy(selectedSetup = setup, holds = state.holds.filterKeys { it in occupied })
        }
    }

    /** Tap cycles a hold through empty -> MID -> START -> END -> empty. No-op on a position with no real hold. */
    fun tapHold(position: GridPosition) {
        _uiState.update { state ->
            if (position !in state.selectedSetup.occupiedPositions()) return@update state
            val holds = state.holds.toMutableMap()
            when (holds[position]) {
                null -> holds[position] = HoldType.MID
                HoldType.MID -> holds[position] = HoldType.START
                HoldType.START -> holds[position] = HoldType.END
                HoldType.END -> holds.remove(position)
            }
            state.copy(holds = holds)
        }
    }

    fun clearBoard() {
        _uiState.update { it.copy(holds = emptyMap()) }
    }

    fun sendToBoard() {
        val holds = _uiState.value.holds.map { (position, type) -> PlacedHold(position, type) }
        AppLogger.log("ViewModel", "sendToBoard: ${holds.size} hold(s) under ${_uiState.value.selectedSetup.displayName}")
        scope.launch { bleClient.sendProblem(holds) }
    }

    /** Exposes state to Swift via a plain callback - Kotlin/Native bridges this to a closure param directly. */
    fun observeState(onChange: (BoardUiState) -> Unit) {
        scope.launch { uiState.collect(onChange) }
    }

    fun onCleared() {
        scope.cancel()
    }
}
