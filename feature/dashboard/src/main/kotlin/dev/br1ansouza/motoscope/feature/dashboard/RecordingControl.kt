package dev.br1ansouza.motoscope.feature.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.br1ansouza.motoscope.core.recording.RecordingState
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopePalette
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeSizes
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
    val haptics = LocalHapticFeedback.current
    val label = stringResource(
        if (active) R.string.dashboard_stop_recording else R.string.dashboard_start_recording
    )
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClickLabel = label,
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (active) onStop() else onStart()
                }
            ),
        shape = MaterialTheme.shapes.large,
        color = if (active) {
            MotoScopeStatusColors.recording
        } else {
            MaterialTheme.colorScheme.primary
        },
        border = BorderStroke(MotoScopeSizes.fieldBorder, MotoScopePalette.gold)
    ) {
        Column(
            modifier = Modifier.heightIn(min = 64.dp).padding(MotoScopeSpacing.small),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MotoScopePalette.ink,
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
                    color = MotoScopePalette.gold,
                    modifier = Modifier.padding(start = MotoScopeSpacing.small)
                )
            }
        }
    }
}
