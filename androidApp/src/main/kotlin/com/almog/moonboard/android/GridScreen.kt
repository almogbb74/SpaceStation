@file:Suppress("FunctionName") // Composable functions should start with an uppercase, so I suppressed the warning for now.

package com.almog.moonboard.android

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.almog.moonboard.android.ui.theme.IbmPlexMono
import com.almog.moonboard.android.ui.theme.MoonBoardAccent
import com.almog.moonboard.android.ui.theme.MoonBoardTextMuted
import com.almog.moonboard.ble.ConnectionState
import com.almog.moonboard.model.BOARD_COLUMNS
import com.almog.moonboard.model.BOARD_ROWS
import com.almog.moonboard.model.BoardSetup
import com.almog.moonboard.model.GridPosition
import com.almog.moonboard.model.HoldType
import com.almog.moonboard.viewmodel.BoardViewModel

@Composable
fun GridScreen(viewModel: BoardViewModel) {
    val state by viewModel.uiState.collectAsState()

    if (state.connectionState !is ConnectionState.Connected) {
        NotConnectedPlaceholder()
        return
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        HoldGrid(state.selectedSetup, state.holds, onTap = viewModel::tapHold)
        Spacer(Modifier.height(12.dp))
        Row {
            Button(onClick = viewModel::clearBoard) { Text(stringResource(R.string.action_clear)) }
            Spacer(Modifier.width(8.dp))
            Button(onClick = viewModel::sendToBoard) { Text(stringResource(R.string.action_light_it_up)) }
        }
    }
}

@Composable
fun NotConnectedPlaceholder() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        RadarPulse()
        Spacer(Modifier.height(20.dp))
        Text(
            stringResource(R.string.not_connected_placeholder).uppercase(),
            fontFamily = IbmPlexMono,
            fontSize = 13.sp,
            letterSpacing = 1.sp,
            color = MoonBoardTextMuted,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun RadarPulse() {
    val transition = rememberInfiniteTransition(label = "radar")
    Box(Modifier.size(96.dp), contentAlignment = Alignment.Center) {
        repeat(3) { index ->
            val progress by transition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2400, easing = LinearEasing),
                    initialStartOffset = StartOffset(index * 800),
                ),
                label = "radar-ring-$index",
            )
            Box(
                Modifier
                    .size(76.dp)
                    .scale(0.4f + progress * 1.4f)
                    .alpha((1f - progress) * 0.7f)
                    .border(1.5.dp, MoonBoardAccent, CircleShape),
            )
        }
        Box(Modifier.size(30.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Bluetooth, contentDescription = null, tint = MoonBoardAccent, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun HoldGrid(setup: BoardSetup, holds: Map<GridPosition, HoldType>, onTap: (GridPosition) -> Unit) {
    val occupied = remember(setup) { setup.occupiedPositions() }
    LazyVerticalGrid(columns = GridCells.Fixed(BOARD_COLUMNS)) {
        items(BOARD_COLUMNS * BOARD_ROWS) { index ->
            val column = index % BOARD_COLUMNS + 1
            val row = BOARD_ROWS - index / BOARD_COLUMNS
            val position = GridPosition(column, row)
            Box(modifier = Modifier.padding(2.dp).size(24.dp), contentAlignment = Alignment.Center) {
                if (position in occupied) {
                    val color = when (holds[position]) {
                        HoldType.START -> Color(0xFF2E7D32)
                        HoldType.MID -> Color(0xFF1565C0)
                        HoldType.END -> Color(0xFFC62828)
                        null -> Color(0xFFE0E0E0)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(color)
                            .clickable { onTap(position) }
                    )
                }
            }
        }
    }
}
