package dev.br1ansouza.motoscope.feature.dashboard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import dev.br1ansouza.motoscope.core.recording.RecordingState
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeSpacing
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeStatusColors

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun RecordingControl(
    state: RecordingState,
    onStart: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val active = state is RecordingState.Active
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { if (!active) onStart() },
                onLongClick = { if (active) onStop() }
            ),
        shape = MaterialTheme.shapes.large,
        color = if (active) MotoScopeStatusColors.failure else MaterialTheme.colorScheme.primary
    ) {
        Column(
            modifier = Modifier.padding(MotoScopeSpacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MotoScopeSpacing.tiny)
        ) {
            Text(
                text = stringResource(
                    if (active) {
                        R.string.dashboard_stop_recording
                    } else {
                        R.string.dashboard_start_recording
                    }
                ),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center
            )
            if (state is RecordingState.Active) {
                val total = (state.samplesWritten + state.samplesPending)
                    .coerceAtMost(Int.MAX_VALUE.toLong())
                    .toInt()
                Text(
                    text = pluralStringResource(
                        R.plurals.dashboard_recording_counter,
                        total,
                        total
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
