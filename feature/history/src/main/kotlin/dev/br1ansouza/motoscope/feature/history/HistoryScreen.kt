package dev.br1ansouza.motoscope.feature.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import dev.br1ansouza.motoscope.core.model.RecordingSession
import dev.br1ansouza.motoscope.core.model.SessionId
import dev.br1ansouza.motoscope.core.model.SessionStatus
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopePalette
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeSizes
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeSpacing
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeTheme

@Composable
fun HistoryScreen(
    state: HistoryState,
    actions: HistoryActions,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.safeDrawingPadding()) {
            HistoryHeader(title = R.string.history_title, onBack = actions.onBack)
            when (state) {
                HistoryState.Loading -> HistoryNotice(R.string.history_loading)
                is HistoryState.Ready -> if (state.sessions.isEmpty()) {
                    HistoryNotice(R.string.history_empty)
                } else {
                    SessionList(state.sessions, actions.onOpenSession)
                }
            }
        }
    }
}

@Composable
private fun SessionList(sessions: List<RecordingSession>, onOpen: (SessionId) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(MotoScopeSpacing.small),
        verticalArrangement = Arrangement.spacedBy(MotoScopeSpacing.small)
    ) {
        items(items = sessions, key = { it.id.value }) { session ->
            SessionRow(session = session, onOpen = onOpen)
        }
    }
}

@Composable
private fun SessionRow(session: RecordingSession, onOpen: (SessionId) -> Unit) {
    val duration = session.endedAtEpochMillis?.minus(session.startedAtEpochMillis)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .border(MotoScopeSizes.fieldBorder, MaterialTheme.colorScheme.outline)
            .clickable { onOpen(session.id) }
            .padding(MotoScopeSpacing.small),
        verticalArrangement = Arrangement.spacedBy(MotoScopeSpacing.tiny)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = HistoryFormatting.startedAt(session.startedAtEpochMillis),
                style = MaterialTheme.typography.titleMedium,
                color = MotoScopePalette.ink
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(MotoScopeSpacing.tiny),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusDot(status = session.status)
                Text(
                    text = stringResource(session.status.label()),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            text = duration?.let(HistoryFormatting::duration)
                ?: stringResource(R.string.history_duration_open),
            style = MaterialTheme.typography.labelLarge,
            color = MotoScopePalette.gold
        )
    }
}

@Composable
private fun StatusDot(status: SessionStatus) {
    Box(
        modifier = Modifier
            .size(MotoScopeSizes.indicatorDot)
            .background(status.color(), RectangleShape)
    )
}

@Composable
internal fun SampleCount(count: Long) {
    val capped = count.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    Text(
        text = pluralStringResource(R.plurals.history_sample_count, capped, capped),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Preview(showBackground = true, widthDp = 400, heightDp = 720)
@Composable
private fun HistoryScreenPreview() {
    MotoScopeTheme {
        HistoryScreen(
            state = HistoryState.Ready(
                listOf(
                    RecordingSession(
                        id = SessionId("a"),
                        status = SessionStatus.FINISHED,
                        startedAtEpochMillis = PREVIEW_START,
                        endedAtEpochMillis = PREVIEW_START + PREVIEW_DURATION
                    ),
                    RecordingSession(
                        id = SessionId("b"),
                        status = SessionStatus.INTERRUPTED,
                        startedAtEpochMillis = PREVIEW_START - PREVIEW_DURATION
                    )
                )
            ),
            actions = HistoryActions(
                onOpenSession = {},
                onDeleteSession = {},
                onBack = {}
            )
        )
    }
}

private const val PREVIEW_START = 1_700_000_000_000
private const val PREVIEW_DURATION = 46_700L
