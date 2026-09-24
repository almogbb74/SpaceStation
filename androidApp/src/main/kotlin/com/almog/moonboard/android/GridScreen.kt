package com.almog.moonboard.android

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.GridOn
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
private fun NotConnectedPlaceholder() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.Default.GridOn,
            contentDescription = null,
            modifier = Modifier.size(96.dp),
            tint = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.not_connected_placeholder), color = MaterialTheme.colorScheme.onBackground)
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
