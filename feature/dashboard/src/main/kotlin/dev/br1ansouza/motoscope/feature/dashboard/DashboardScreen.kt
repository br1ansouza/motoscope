package dev.br1ansouza.motoscope.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.br1ansouza.motoscope.core.model.EcuState
import dev.br1ansouza.motoscope.core.model.Freshness
import dev.br1ansouza.motoscope.core.model.MetricUnit
import dev.br1ansouza.motoscope.core.model.TelemetryMetric
import dev.br1ansouza.motoscope.core.model.TelemetrySample
import dev.br1ansouza.motoscope.core.model.TelemetrySource
import dev.br1ansouza.motoscope.core.model.TransportState
import dev.br1ansouza.motoscope.core.recording.RecordingState
import dev.br1ansouza.motoscope.core.telemetry.LiveTelemetry
import dev.br1ansouza.motoscope.core.telemetry.MetricReading
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeSpacing
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeTheme

@Composable
fun DashboardScreen(
    state: LiveTelemetry,
    recording: RecordingState,
    settings: DashboardSettings,
    actions: DashboardActions,
    modifier: Modifier = Modifier
) {
    var menuOpen by rememberSaveable { mutableStateOf(false) }
    Surface(modifier = modifier.fillMaxSize()) {
        Row(modifier = Modifier.safeDrawingPadding()) {
            if (menuOpen) {
                ConfigurationPanel(
                    settings = settings,
                    actions = actions,
                    onClose = { menuOpen = false }
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(MotoScopeSpacing.large),
                verticalArrangement = Arrangement.spacedBy(MotoScopeSpacing.small)
            ) {
                IndicatorRow(
                    leading = listOf(state.transport.indicator(), state.ecu.indicator()),
                    trailing = listOf(recording.indicator())
                )
                if (state.simulated) {
                    SimulationNotice()
                }
                DashboardBody(
                    state = state,
                    settings = settings,
                    modifier = Modifier.weight(1f)
                )
                Row(
                    modifier = Modifier.height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(MotoScopeSpacing.small)
                ) {
                    MetricsButton(onClick = { menuOpen = !menuOpen })
                    RecordingControl(
                        state = recording,
                        onStart = actions.onStartRecording,
                        onStop = actions.onStopRecording,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 720)
@Composable
private fun DashboardScreenPreview() {
    MotoScopeTheme {
        DashboardScreen(
            state = previewState(),
            recording = RecordingState.Idle,
            settings = DashboardSettings(),
            actions = DashboardActions(
                onToggleMetric = { _, _ -> },
                onSelectLayout = {},
                onSelectVehicle = {},
                onStartRecording = {},
                onStopRecording = {}
            )
        )
    }
}

private fun previewState() = LiveTelemetry(
    transport = TransportState.CONNECTED,
    ecu = EcuState.RESPONDING,
    simulated = true,
    readings = mapOf(
        TelemetryMetric.ENGINE_RPM to reading(
            TelemetryMetric.ENGINE_RPM,
            PREVIEW_RPM,
            MetricUnit.REVOLUTIONS_PER_MINUTE,
            Freshness.FRESH
        ),
        TelemetryMetric.ENGINE_TEMPERATURE to reading(
            TelemetryMetric.ENGINE_TEMPERATURE,
            PREVIEW_TEMPERATURE,
            MetricUnit.DEGREE_CELSIUS,
            Freshness.FRESH
        ),
        TelemetryMetric.SYSTEM_VOLTAGE to reading(
            TelemetryMetric.SYSTEM_VOLTAGE,
            PREVIEW_VOLTAGE,
            MetricUnit.VOLT,
            Freshness.DELAYED
        ),
        TelemetryMetric.THROTTLE_POSITION to reading(
            TelemetryMetric.THROTTLE_POSITION,
            PREVIEW_THROTTLE,
            MetricUnit.PERCENT,
            Freshness.ABSENT
        )
    )
)

private fun reading(
    metric: TelemetryMetric,
    value: Double,
    unit: MetricUnit,
    freshness: Freshness
) = MetricReading(
    sample = TelemetrySample(
        metric = metric,
        value = value,
        unit = unit,
        source = TelemetrySource.ECU,
        monotonicMillis = 1_000,
        wallClockEpochMillis = 1_700_000_000_000
    ),
    freshness = freshness
)

private const val PREVIEW_RPM = 3_420.0
private const val PREVIEW_TEMPERATURE = 92.0
private const val PREVIEW_VOLTAGE = 14.2
private const val PREVIEW_THROTTLE = 18.0
