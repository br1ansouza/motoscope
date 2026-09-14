package dev.br1ansouza.motoscope.feature.history

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import dev.br1ansouza.motoscope.core.history.ExportFormat
import dev.br1ansouza.motoscope.core.model.MetricSummary
import dev.br1ansouza.motoscope.core.model.MetricUnit
import dev.br1ansouza.motoscope.core.model.RecordingSession
import dev.br1ansouza.motoscope.core.model.SessionEvent
import dev.br1ansouza.motoscope.core.model.SessionEventType
import dev.br1ansouza.motoscope.core.model.SessionId
import dev.br1ansouza.motoscope.core.model.SessionStatus
import dev.br1ansouza.motoscope.core.model.SessionSummary
import dev.br1ansouza.motoscope.core.model.TelemetryMetric
import dev.br1ansouza.motoscope.core.ui.labels
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopePalette
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeSizes
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeSpacing
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeTheme

@Composable
fun SessionDetailScreen(
    state: SessionDetailState,
    actions: HistoryActions,
    modifier: Modifier = Modifier,
    notice: ExportNotice? = null
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.safeDrawingPadding()) {
            HistoryHeader(title = R.string.history_detail_title, onBack = actions.onBack)
            when (state) {
                SessionDetailState.Loading -> HistoryNotice(R.string.history_loading)
                SessionDetailState.Missing -> HistoryNotice(R.string.history_detail_missing)
                is SessionDetailState.Ready -> DetailBody(state, actions, notice)
            }
        }
    }
}

@Composable
private fun DetailBody(
    state: SessionDetailState.Ready,
    actions: HistoryActions,
    notice: ExportNotice?
) {
    val summary = state.summary
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(MotoScopeSpacing.small),
        verticalArrangement = Arrangement.spacedBy(MotoScopeSpacing.small)
    ) {
        item { SessionHeadline(summary) }
        if (summary.metrics.isEmpty()) {
            item { HistoryNotice(R.string.history_no_metrics) }
        } else {
            item { MetricTableHeader() }
            items(items = summary.metrics, key = { it.metric.name }) { MetricRow(it) }
        }
        item { SectionTitle(R.string.history_events_title) }
        items(items = state.events) { EventRow(it) }
        item { SectionTitle(R.string.history_export_title) }
        item {
            ExportRow(
                enabled = summary.sampleCount > 0,
                onExport = { actions.onExportSession(summary.session.id, it) }
            )
        }
        if (notice != null) {
            item { HistoryNotice(notice.label()) }
        }
        item {
            HoldToConfirm(
                idle = R.string.history_delete,
                held = R.string.history_delete_confirm,
                onConfirm = { actions.onDeleteSession(summary.session.id) }
            )
        }
    }
}

@Composable
private fun SessionHeadline(summary: SessionSummary) {
    val session = summary.session
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .border(MotoScopeSizes.fieldBorder, MaterialTheme.colorScheme.outline)
            .padding(MotoScopeSpacing.small),
        verticalArrangement = Arrangement.spacedBy(MotoScopeSpacing.tiny)
    ) {
        Text(
            text = HistoryFormatting.startedAt(session.startedAtEpochMillis),
            style = MaterialTheme.typography.titleMedium,
            color = MotoScopePalette.ink
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = summary.durationMillis?.let(HistoryFormatting::duration)
                    ?: stringResource(R.string.history_duration_open),
                style = MaterialTheme.typography.labelLarge,
                color = MotoScopePalette.gold
            )
            Text(
                text = stringResource(session.status.label()),
                style = MaterialTheme.typography.labelLarge,
                color = session.status.color()
            )
        }
        SampleCount(summary.sampleCount)
    }
}

@Composable
private fun SectionTitle(@StringRes title: Int) {
    Text(
        text = stringResource(title),
        style = MaterialTheme.typography.labelLarge,
        color = MotoScopePalette.gold,
        modifier = Modifier.padding(top = MotoScopeSpacing.small)
    )
}

@Composable
private fun EventRow(event: SessionEvent) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = MotoScopeSpacing.tiny),
        horizontalArrangement = Arrangement.spacedBy(MotoScopeSpacing.small)
    ) {
        Text(
            text = HistoryFormatting.clock(event.wallClockEpochMillis),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(event.type.label()),
            style = MaterialTheme.typography.labelLarge,
            color = MotoScopePalette.ink
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 720)
@Composable
private fun SessionDetailPreview() {
    MotoScopeTheme {
        SessionDetailScreen(
            state = SessionDetailState.Ready(
                summary = SessionSummary(
                    session = RecordingSession(
                        id = SessionId("a"),
                        status = SessionStatus.FINISHED,
                        startedAtEpochMillis = PREVIEW_START,
                        endedAtEpochMillis = PREVIEW_START + PREVIEW_DURATION
                    ),
                    sampleCount = 743,
                    metrics = listOf(
                        MetricSummary(
                            metric = TelemetryMetric.ENGINE_RPM,
                            unit = MetricUnit.REVOLUTIONS_PER_MINUTE,
                            count = 400,
                            minimum = 1_200.0,
                            maximum = 8_400.0,
                            average = 3_420.0
                        ),
                        MetricSummary(
                            metric = TelemetryMetric.ENGINE_TEMPERATURE,
                            unit = MetricUnit.DEGREE_CELSIUS,
                            count = 343,
                            minimum = 41.0,
                            maximum = 98.0,
                            average = 88.5
                        )
                    )
                ),
                events = listOf(
                    SessionEvent(
                        type = SessionEventType.RECORDING_STARTED,
                        monotonicMillis = 0,
                        wallClockEpochMillis = PREVIEW_START
                    ),
                    SessionEvent(
                        type = SessionEventType.RECORDING_FINISHED,
                        monotonicMillis = PREVIEW_DURATION,
                        wallClockEpochMillis = PREVIEW_START + PREVIEW_DURATION
                    )
                )
            ),
            actions = HistoryActions(
                onOpenSession = {},
                onDeleteSession = {},
                onExportSession = { _, _ -> },
                onBack = {}
            )
        )
    }
}

private const val PREVIEW_START = 1_700_000_000_000
private const val PREVIEW_DURATION = 46_700L
