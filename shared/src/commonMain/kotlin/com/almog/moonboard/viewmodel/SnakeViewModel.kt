package com.almog.moonboard.viewmodel

import com.almog.moonboard.ble.ConnectionState
import com.almog.moonboard.ble.MoonBoardBleClient
import com.almog.moonboard.model.BOARD_COLUMNS
import com.almog.moonboard.model.BOARD_ROWS
import com.almog.moonboard.model.GridPosition
import com.almog.moonboard.model.HoldType
import com.almog.moonboard.model.PlacedHold
import com.almog.moonboard.snake.Direction
import com.almog.moonboard.snake.INITIAL_DIRECTION
import com.almog.moonboard.snake.INITIAL_SNAKE
import com.almog.moonboard.snake.SnakeEngine
import com.almog.moonboard.snake.SpiralOrder
import com.almog.moonboard.snake.StepResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TICK_INTERVAL_MS = 400L
private const val BLINK_ON_MS = 400L
private const val BLINK_OFF_MS = 200L
private const val SPIRAL_BATCH_SIZE = BOARD_COLUMNS

enum class GameStatus { IDLE, RUNNING, DYING, GAME_OVER }

data class SnakeUiState(
    val connectionState: ConnectionState = ConnectionState.Disconnected,
    val status: GameStatus = GameStatus.IDLE,
    val snake: List<GridPosition> = emptyList(),
    val food: GridPosition? = null,
    val score: Int = 0,
)

class SnakeViewModel(private val bleClient: MoonBoardBleClient) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _uiState = MutableStateFlow(SnakeUiState())
    val uiState: StateFlow<SnakeUiState> = _uiState.asStateFlow()

    private var gameJob: Job? = null
    private var pendingDirection: Direction? = null

    init {
        scope.launch {
            bleClient.connectionState.collect { connection ->
                _uiState.update { it.copy(connectionState = connection) }
                if (connection !is ConnectionState.Connected) stopGame()
            }
        }
    }

    fun start() {
        if (_uiState.value.connectionState !is ConnectionState.Connected) return
        pendingDirection = null
        val food = SnakeEngine.randomFood(INITIAL_SNAKE)
        _uiState.update { it.copy(status = GameStatus.RUNNING, snake = INITIAL_SNAKE, food = food, score = 0) }
        gameJob?.cancel()
        gameJob = scope.launch { runLoop() }
    }

    fun queueDirection(direction: Direction) {
        if (_uiState.value.status != GameStatus.RUNNING) return
        pendingDirection = direction
    }

    /** Called when the game should end without a death animation: leaving the screen, or a disconnect. */
    fun stopGame() {
        gameJob?.cancel()
        gameJob = null
        if (_uiState.value.status != GameStatus.IDLE) {
            _uiState.update { it.copy(status = GameStatus.IDLE, snake = emptyList(), food = null, score = 0) }
        }
    }

    private suspend fun runLoop() {
        var direction = INITIAL_DIRECTION
        while (true) {
            pendingDirection?.let { queued ->
                if (!queued.isOpposite(direction)) direction = queued
                pendingDirection = null
            }
            val state = _uiState.value
            val food = state.food ?: return
            when (val result = SnakeEngine.step(state.snake, direction, food)) {
                is StepResult.Died -> {
                    _uiState.update { it.copy(status = GameStatus.DYING) }
                    playDeathSequence(toHolds(state.snake, food))
                    _uiState.update { it.copy(status = GameStatus.GAME_OVER) }
                    return
                }
                is StepResult.Moved -> {
                    val newFood = if (result.ateFood) SnakeEngine.randomFood(result.snake) else food
                    val newScore = state.score + if (result.ateFood) 1 else 0
                    _uiState.update { it.copy(snake = result.snake, food = newFood, score = newScore) }
                    bleClient.sendProblem(toHolds(result.snake, newFood))
                }
            }
            delay(TICK_INTERVAL_MS)
        }
    }

    private suspend fun playDeathSequence(deathFrame: List<PlacedHold>) {
        repeat(3) {
            bleClient.sendProblem(deathFrame)
            delay(BLINK_ON_MS)
            bleClient.sendProblem(emptyList())
            delay(BLINK_OFF_MS)
        }
        val lit = mutableListOf<PlacedHold>()
        SpiralOrder.forGrid(BOARD_COLUMNS, BOARD_ROWS).chunked(SPIRAL_BATCH_SIZE).forEach { batch ->
            lit += batch.map { PlacedHold(it, HoldType.END) }
            bleClient.sendProblem(lit)
        }
        bleClient.sendProblem(emptyList())
    }

    private fun toHolds(snake: List<GridPosition>, food: GridPosition): List<PlacedHold> {
        val head = PlacedHold(snake.first(), HoldType.START)
        val body = snake.drop(1).map { PlacedHold(it, HoldType.MID) }
        return body + head + PlacedHold(food, HoldType.END)
    }

    fun onCleared() {
        scope.cancel()
    }
}
