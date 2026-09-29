@file:Suppress("FunctionName")

package com.almog.moonboard.android

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.almog.moonboard.android.ui.theme.UnboundedExtraBold
import com.almog.moonboard.ble.ConnectionState
import com.almog.moonboard.snake.Direction
import com.almog.moonboard.viewmodel.GameStatus
import com.almog.moonboard.viewmodel.SnakeViewModel

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
                score = state.score,
                highScore = state.highScore,
                interactive = state.status == GameStatus.RUNNING,
                onDirection = viewModel::queueDirection,
            )
        }
        if (state.status == GameStatus.DYING || state.status == GameStatus.GAME_OVER) {
            GameOverOverlay(score = state.score, highScore = state.highScore, onRestart = viewModel::start)
        }
    }
}

private val SnakeHeadColor = Color(0xFF2E7D32) // same green as GridScreen's HoldType.START dot
private val SnakeBodyColor = Color(0xFF1565C0) // same blue as HoldType.MID

@Composable
private fun SnakeIdleContent(onStart: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        ImmersiveBackground(Modifier.fillMaxSize())
        Column(
            Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                stringResource(R.string.snake_title),
                style = TextStyle(
                    fontFamily = UnboundedExtraBold,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 40.sp,
                    letterSpacing = 0.5.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    shadow = Shadow(color = SnakeHeadColor.copy(alpha = 0.6f), blurRadius = 48f),
                ),
            )
            Spacer(Modifier.height(28.dp))
            Button(
                onClick = onStart,
                shape = RoundedCornerShape(percent = 50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.background,
                ),
                contentPadding = PaddingValues(horizontal = 40.dp, vertical = 14.dp),
                modifier = Modifier.shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(percent = 50),
                    ambientColor = MaterialTheme.colorScheme.primary,
                    spotColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.action_start), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ImmersiveBackground(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "snake-path-trace")
    val dashPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = -64f,
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing)),
        label = "dash-phase",
    )
    val dotColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f)
    Canvas(modifier = modifier) {
        val spacing = 16.dp.toPx()
        var y = spacing / 2
        while (y < size.height) {
            var x = spacing / 2
            while (x < size.width) {
                drawCircle(color = dotColor, radius = 1.3f, center = Offset(x, y))
                x += spacing
            }
            y += spacing
        }

        val path = Path().apply {
            moveTo(size.width * 0.12f, size.height * 0.18f)
            cubicTo(
                size.width * 0.6f, size.height * 0.18f,
                size.width * 0.3f, size.height * 0.42f,
                size.width * 0.75f, size.height * 0.42f,
            )
            cubicTo(
                size.width * 1.05f, size.height * 0.42f,
                size.width * 0.85f, size.height * 0.68f,
                size.width * 0.5f, size.height * 0.68f,
            )
            cubicTo(
                size.width * 0.22f, size.height * 0.68f,
                size.width * 0.3f, size.height * 0.9f,
                size.width * 0.68f, size.height * 0.95f,
            )
        }
        drawPath(
            path = path,
            color = SnakeBodyColor,
            alpha = 0.8f,
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 18f), dashPhase),
            ),
        )
    }
}

@Composable
private fun SnakeGameContent(
    score: Int,
    highScore: Int,
    interactive: Boolean,
    onDirection: (Direction) -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Text(stringResource(R.string.snake_score, score), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.snake_best_score, highScore), style = MaterialTheme.typography.titleMedium)
        }
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            DirectionPad(enabled = interactive, onDirection = onDirection)
        }
    }
}

private val DirectionButtonSize = 130.dp
private val DirectionIconSize = 72.dp

@Composable
private fun DirectionPad(enabled: Boolean, onDirection: (Direction) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        DirectionButton(Icons.Default.KeyboardArrowUp, stringResource(R.string.snake_direction_up), enabled) {
            onDirection(Direction.UP)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(56.dp)) {
            DirectionButton(Icons.AutoMirrored.Filled.KeyboardArrowLeft, stringResource(R.string.snake_direction_left), enabled) {
                onDirection(Direction.LEFT)
            }
            DirectionButton(Icons.AutoMirrored.Filled.KeyboardArrowRight, stringResource(R.string.snake_direction_right), enabled) {
                onDirection(Direction.RIGHT)
            }
        }
        DirectionButton(Icons.Default.KeyboardArrowDown, stringResource(R.string.snake_direction_down), enabled) {
            onDirection(Direction.DOWN)
        }
    }
}

@Composable
private fun DirectionButton(icon: ImageVector, contentDescription: String, enabled: Boolean, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(DirectionButtonSize).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(DirectionIconSize))
    }
}

@Composable
private fun GameOverOverlay(score: Int, highScore: Int, onRestart: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.snake_game_over), style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.snake_score, score), color = Color.White)
            Text(stringResource(R.string.snake_best_score, highScore), color = Color.White)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRestart) { Text(stringResource(R.string.action_restart)) }
        }
    }
}
