@file:Suppress("FunctionName")

package com.almog.moonboard.android

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.almog.moonboard.ble.ConnectionState
import com.almog.moonboard.model.BOARD_COLUMNS
import com.almog.moonboard.model.BOARD_ROWS
import com.almog.moonboard.model.GridPosition
import com.almog.moonboard.snake.Direction
import com.almog.moonboard.viewmodel.GameStatus
import com.almog.moonboard.viewmodel.SnakeViewModel
import kotlin.math.sin

@Composable
fun SnakeScreen(viewModel: SnakeViewModel) {
    DisposableEffect(Unit) {
        onDispose { viewModel.stopGame() }
    }

    val state by viewModel.uiState.collectAsState()
    if (state.connectionState !is ConnectionState.Connected) {
        NotConnectedPlaceholder()
        return
    }
    Box(Modifier.fillMaxSize()) {
        if (state.status == GameStatus.IDLE) {
            SnakeIdleContent(onStart = viewModel::start)
        } else {
            SnakeGameContent(
                snake = state.snake,
                food = state.food,
                score = state.score,
                interactive = state.status == GameStatus.RUNNING,
                onDirection = viewModel::queueDirection,
            )
        }
        if (state.status == GameStatus.GAME_OVER) {
            GameOverOverlay(score = state.score, onRestart = viewModel::start)
        }
    }
}

@Composable
private fun SnakeIdleContent(onStart: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        UndulatingTitle(stringResource(R.string.snake_title))
        Spacer(Modifier.height(24.dp))
        Button(onClick = onStart) { Text(stringResource(R.string.action_start)) }
    }
}

@Composable
private fun UndulatingTitle(text: String) {
    val transition = rememberInfiniteTransition(label = "snake-title")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)),
        label = "phase",
    )
    Row {
        text.forEachIndexed { index, char ->
            val offsetY = sin(phase + index * 0.6f) * 6f
            Text(
                char.toString(),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.offset(y = offsetY.dp),
            )
        }
    }
}

@Composable
private fun SnakeGameContent(
    snake: List<GridPosition>,
    food: GridPosition?,
    score: Int,
    interactive: Boolean,
    onDirection: (Direction) -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.snake_score, score), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        SnakeGrid(snake, food, Modifier.weight(1f))
        Spacer(Modifier.height(16.dp))
        DirectionPad(enabled = interactive, onDirection = onDirection)
    }
}

private val SnakeHeadColor = Color(0xFF2E7D32) // same green as GridScreen's HoldType.START dot
private val SnakeBodyColor = Color(0xFF1565C0) // same blue as HoldType.MID
private val SnakeFoodColor = Color(0xFFC62828) // same red as HoldType.END

@Composable
private fun SnakeGrid(snake: List<GridPosition>, food: GridPosition?, modifier: Modifier = Modifier) {
    val snakeSet = remember(snake) { snake.toSet() }
    val head = snake.firstOrNull()
    LazyVerticalGrid(columns = GridCells.Fixed(BOARD_COLUMNS), modifier = modifier, userScrollEnabled = false) {
        items(BOARD_COLUMNS * BOARD_ROWS) { index ->
            val column = index % BOARD_COLUMNS + 1
            val row = BOARD_ROWS - index / BOARD_COLUMNS
            val position = GridPosition(column, row)
            val color = when {
                position == head -> SnakeHeadColor
                position == food -> SnakeFoodColor
                position in snakeSet -> SnakeBodyColor
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
            Box(Modifier.padding(1.dp).size(16.dp).clip(CircleShape).background(color))
        }
    }
}

@Composable
private fun DirectionPad(enabled: Boolean, onDirection: (Direction) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = { onDirection(Direction.UP) }, enabled = enabled) {
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = stringResource(R.string.snake_direction_up))
        }
        Row {
            IconButton(onClick = { onDirection(Direction.LEFT) }, enabled = enabled) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = stringResource(R.string.snake_direction_left))
            }
            Spacer(Modifier.width(48.dp))
            IconButton(onClick = { onDirection(Direction.RIGHT) }, enabled = enabled) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = stringResource(R.string.snake_direction_right))
            }
        }
        IconButton(onClick = { onDirection(Direction.DOWN) }, enabled = enabled) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = stringResource(R.string.snake_direction_down))
        }
    }
}

@Composable
private fun GameOverOverlay(score: Int, onRestart: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.snake_game_over), style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.snake_score, score), color = Color.White)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRestart) { Text(stringResource(R.string.action_restart)) }
        }
    }
}
