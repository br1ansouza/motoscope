package dev.br1ansouza.motoscope.feature.dashboard

import androidx.annotation.StringRes
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeReadingColors
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeSizes
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
            verticalArrangement = Arrangement.spacedBy(MotoScopeSpacing.small)
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(MotoScopeSizes.fieldBorder, MotoScopeStatusColors.warning)
                        .padding(MotoScopeSpacing.tiny),
                    textAlign = TextAlign.Center
                )
            }
            PrimaryReading(
                reading = state.reading(TelemetryMetric.ENGINE_RPM),
                modifier = Modifier.weight(1f)
            )
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
private fun PrimaryReading(reading: MetricReading?, modifier: Modifier = Modifier) {
    Field(
        label = R.string.dashboard_primary_label,
        modifier = modifier.fillMaxWidth(),
        fillHeight = true
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MotoScopeSpacing.medium)
        ) {
            FieldValue(
                value = reading.display(),
                unit = null,
                color = reading.freshnessColor(),
                large = true
            )
            RpmBar(fraction = reading.rpmFraction())
        }
    }
}

private fun MetricReading?.rpmFraction(): Float {
    if (this == null || freshness == Freshness.ABSENT) return 0f
    return (sample.value / RPM_SCALE_MAX).toFloat()
}

@Composable
private fun SecondaryRow(state: LiveTelemetry) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MotoScopeSpacing.small)
    ) {
        SecondaryReading(
            label = R.string.dashboard_engine_temperature,
            unit = R.string.dashboard_unit_celsius,
            reading = state.reading(TelemetryMetric.ENGINE_TEMPERATURE),
            modifier = Modifier.weight(1f)
        )
        SecondaryReading(
            label = R.string.dashboard_system_voltage,
            unit = R.string.dashboard_unit_volt,
            reading = state.reading(TelemetryMetric.SYSTEM_VOLTAGE),
            modifier = Modifier.weight(1f)
        )
        SecondaryReading(
            label = R.string.dashboard_throttle,
            unit = R.string.dashboard_unit_percent,
            reading = state.reading(TelemetryMetric.THROTTLE_POSITION),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SecondaryReading(
    @StringRes label: Int,
    @StringRes unit: Int,
    reading: MetricReading?,
    modifier: Modifier = Modifier
) {
    Field(label = label, modifier = modifier) {
        FieldValue(
            value = reading.display(),
            unit = if (reading?.freshness == Freshness.ABSENT) null else stringResource(unit),
            color = reading.freshnessColor(),
            large = false
        )
    }
}

@Composable
private fun MetricReading?.display(): String = if (this == null || freshness == Freshness.ABSENT) {
    stringResource(R.string.dashboard_absent_value)
} else {
    MetricFormatting.format(sample.value, sample.unit)
}

private fun MetricReading?.freshnessColor(): Color = when (this?.freshness) {
    null -> MotoScopeReadingColors.absent
    Freshness.FRESH -> MotoScopeReadingColors.fresh
    Freshness.DELAYED -> MotoScopeReadingColors.delayed
    Freshness.ABSENT -> MotoScopeReadingColors.absent
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
