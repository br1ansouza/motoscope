package dev.br1ansouza.motoscope.feature.history

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import dev.br1ansouza.motoscope.core.model.SessionEventType
import dev.br1ansouza.motoscope.core.model.SessionStatus
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopePalette
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeSizes
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeSpacing
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeStatusColors

@Composable
internal fun HistoryHeader(
    @StringRes title: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MotoScopePalette.graphiteRaised)
            .padding(horizontal = MotoScopeSpacing.small, vertical = MotoScopeSpacing.tiny),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(title),
            style = MaterialTheme.typography.labelLarge,
            color = MotoScopePalette.gold
        )
        Text(
            text = stringResource(R.string.history_back),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .border(MotoScopeSizes.fieldBorder, MaterialTheme.colorScheme.outline)
                .clickable(onClick = onBack)
                .padding(horizontal = MotoScopeSpacing.small, vertical = MotoScopeSpacing.tiny)
        )
    }
}

@Composable
internal fun HistoryNotice(@StringRes message: Int, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(message),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(MotoScopeSpacing.large)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun HoldToConfirm(
    @StringRes idle: Int,
    @StringRes held: Int,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    val label = stringResource(held)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClickLabel = label,
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onConfirm()
                }
            ),
        shape = MaterialTheme.shapes.large,
        color = MotoScopeStatusColors.failure,
        border = BorderStroke(MotoScopeSizes.fieldBorder, MotoScopePalette.gold)
    ) {
        Column(
            modifier = Modifier.padding(MotoScopeSpacing.small),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(idle),
                style = MaterialTheme.typography.labelLarge,
                color = MotoScopePalette.ink,
                textAlign = TextAlign.Center
            )
        }
    }
}

@StringRes
internal fun SessionStatus.label(): Int = when (this) {
    SessionStatus.RECORDING -> R.string.history_status_recording
    SessionStatus.FINISHED -> R.string.history_status_finished
    SessionStatus.INTERRUPTED -> R.string.history_status_interrupted
}

internal fun SessionStatus.color() = when (this) {
    SessionStatus.RECORDING -> MotoScopeStatusColors.recording
    SessionStatus.FINISHED -> MotoScopeStatusColors.ok
    SessionStatus.INTERRUPTED -> MotoScopeStatusColors.warning
}

@StringRes
internal fun SessionEventType.label(): Int = when (this) {
    SessionEventType.RECORDING_STARTED -> R.string.history_event_recording_started
    SessionEventType.RECORDING_RESUMED -> R.string.history_event_recording_resumed
    SessionEventType.RECORDING_INTERRUPTED -> R.string.history_event_recording_interrupted
    SessionEventType.RECORDING_FINISHED -> R.string.history_event_recording_finished
    SessionEventType.TRANSPORT_LOST -> R.string.history_event_transport_lost
    SessionEventType.TRANSPORT_RECOVERED -> R.string.history_event_transport_recovered
    SessionEventType.DATA_GAP_STARTED -> R.string.history_event_data_gap_started
    SessionEventType.DATA_GAP_ENDED -> R.string.history_event_data_gap_ended
}
