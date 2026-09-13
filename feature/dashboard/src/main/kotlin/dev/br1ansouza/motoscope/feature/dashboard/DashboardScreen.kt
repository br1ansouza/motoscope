package dev.br1ansouza.motoscope.feature.dashboard

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
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
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeStatusColors
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeTheme

@Composable
fun DashboardScreen(
    state: LiveTelemetry,
    recording: RecordingState,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .safeDrawingPadding()
                .padding(MotoScopeSpacing.large),
            verticalArrangement = Arrangement.spacedBy(MotoScopeSpacing.large)
        ) {
            IndicatorRow(
                leading = listOf(state.transport.indicator(), state.ecu.indicator()),
                trailing = listOf(recording.indicator())
            )
            if (state.simulated) {
                Text(
                    text = stringResource(R.string.dashboard_simulated),
                    style = MaterialTheme.typography.labelLarge,
                    color = MotoScopeStatusColors.warning,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            PrimaryReading(reading = state.reading(TelemetryMetric.ENGINE_RPM))
            Spacer(modifier = Modifier.weight(1f))
            SecondaryRow(state = state)
            RecordingControl(
                state = recording,
                onStart = onStartRecording,
                onStop = onStopRecording
            )
        }
    }
}

@Composable
private fun PrimaryReading(reading: MetricReading?) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = reading.display(),
            style = MaterialTheme.typography.displayLarge,
            color = reading.freshnessColor()
        )
        Text(
            text = stringResource(R.string.dashboard_primary_label),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SecondaryRow(state: LiveTelemetry) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SecondaryReading(
            label = R.string.dashboard_engine_temperature,
            unit = R.string.dashboard_unit_celsius,
            reading = state.reading(TelemetryMetric.ENGINE_TEMPERATURE)
        )
        SecondaryReading(
            label = R.string.dashboard_system_voltage,
            unit = R.string.dashboard_unit_volt,
            reading = state.reading(TelemetryMetric.SYSTEM_VOLTAGE)
        )
        SecondaryReading(
            label = R.string.dashboard_throttle,
            unit = R.string.dashboard_unit_percent,
            reading = state.reading(TelemetryMetric.THROTTLE_POSITION)
        )
    }
}

@Composable
private fun SecondaryReading(@StringRes label: Int, @StringRes unit: Int, reading: MetricReading?) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = stringResource(label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = reading.display(),
                style = MaterialTheme.typography.displaySmall,
                color = reading.freshnessColor()
            )
            if (reading?.freshness != Freshness.ABSENT) {
                Text(
                    text = stringResource(unit),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = MotoScopeSpacing.tiny)
                )
            }
        }
    }
}

@Composable
private fun MetricReading?.display(): String = if (this == null || freshness == Freshness.ABSENT) {
    stringResource(R.string.dashboard_absent_value)
} else {
    MetricFormatting.format(sample.value, sample.unit)
}

private fun MetricReading?.freshnessColor(): Color = when (this?.freshness) {
    null -> MotoScopeStatusColors.disabled
    Freshness.FRESH -> MotoScopeStatusColors.ok
    Freshness.DELAYED -> MotoScopeStatusColors.warning
    Freshness.ABSENT -> MotoScopeStatusColors.disabled
}

@Preview(showBackground = true, widthDp = 400, heightDp = 720)
@Composable
private fun DashboardScreenPreview() {
    MotoScopeTheme {
        DashboardScreen(
            state = previewState(),
            recording = RecordingState.Idle,
            onStartRecording = {},
            onStopRecording = {}
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
