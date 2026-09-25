@file:Suppress("FunctionName") // Composable functions should start with an uppercase, so I suppressed the warning for now.

package com.almog.moonboard.android

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.almog.moonboard.debug.AppLogger
import com.almog.moonboard.model.BoardSetup
import com.almog.moonboard.viewmodel.BoardViewModel

@Composable
fun SettingsScreen(viewModel: BoardViewModel) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        Text(stringResource(R.string.board_setup_heading), style = MaterialTheme.typography.titleMedium)
        BoardSetup.entries.forEach { setup ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.selectSetup(setup) }
                    .padding(vertical = 8.dp)
            ) {
                RadioButton(selected = setup == state.selectedSetup, onClick = { viewModel.selectSetup(setup) })
                Text(setup.displayName)
            }
        }

        Spacer(Modifier.height(24.dp))
        Text(stringResource(R.string.debug_log_heading), style = MaterialTheme.typography.titleMedium)
        Text(
            stringResource(R.string.debug_log_description),
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(8.dp))
        val shareLogChooserTitle = stringResource(R.string.share_log_chooser_title)
        Row {
            Button(onClick = { shareLogFile(context, shareLogChooserTitle) }) { Text(stringResource(R.string.action_share_log)) }
            Spacer(Modifier.width(8.dp))
            Button(onClick = { AppLogger.currentLogFile()?.writeText("") }) { Text(stringResource(R.string.action_clear_log)) }
        }
    }
}

private fun shareLogFile(context: Context, chooserTitle: String) {
    val file = AppLogger.currentLogFile() ?: return
    if (!file.exists()) return
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, chooserTitle))
}
