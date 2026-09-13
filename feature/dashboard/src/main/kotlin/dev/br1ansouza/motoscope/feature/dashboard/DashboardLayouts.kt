package dev.br1ansouza.motoscope.feature.dashboard

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import dev.br1ansouza.motoscope.core.model.Freshness
import dev.br1ansouza.motoscope.core.model.TelemetryMetric
import dev.br1ansouza.motoscope.core.settings.DashboardLayout
import dev.br1ansouza.motoscope.core.settings.DashboardMetrics
import dev.br1ansouza.motoscope.core.telemetry.LiveTelemetry
import dev.br1ansouza.motoscope.core.telemetry.MetricReading
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeReadingColors
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeSizes
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeSpacing
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeStatusColors
import dev.br1ansouza.motoscope.core.vehicle.EngineProfile

@Composable
internal fun DashboardBody(
    state: LiveTelemetry,
    settings: DashboardSettings,
    modifier: Modifier = Modifier
) {
    val ordered = DashboardMetrics.SELECTABLE.filter { it in settings.visibleMetrics }
    val engine = settings.vehicle.engine
    when (settings.layout) {
        DashboardLayout.PRIMARY_TOP -> Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(MotoScopeSpacing.small)
        ) {
            PrimaryReading(
                reading = state.reading(TelemetryMetric.ENGINE_RPM),
                engine = engine,
                modifier = Modifier.weight(1f)
            )
            MetricGrid(state = state, metrics = ordered)
        }

        DashboardLayout.PRIMARY_CENTERED -> Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(MotoScopeSpacing.small)
        ) {
            val half = (ordered.size + 1) / 2
            MetricGrid(state = state, metrics = ordered.take(half))
            PrimaryReading(
                reading = state.reading(TelemetryMetric.ENGINE_RPM),
                engine = engine,
                modifier = Modifier.weight(1f)
            )
            MetricGrid(state = state, metrics = ordered.drop(half))
        }

        DashboardLayout.UNIFORM -> UniformGrid(
            state = state,
            metrics = ordered,
            engine = engine,
            modifier = modifier
        )
    }
}

@Composable
private fun UniformGrid(
    state: LiveTelemetry,
    metrics: List<TelemetryMetric>,
    engine: EngineProfile,
    modifier: Modifier = Modifier
) {
    val cells = listOf(TelemetryMetric.ENGINE_RPM) + metrics
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MotoScopeSpacing.small)
    ) {
        cells.chunked(COLUMNS).forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(MotoScopeSpacing.small)
            ) {
                row.forEach { metric ->
                    if (metric == TelemetryMetric.ENGINE_RPM) {
                        PrimaryReading(
                            reading = state.reading(metric),
                            engine = engine,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            compact = true
                        )
                    } else {
                        MetricField(
                            metric = metric,
                            reading = state.reading(metric),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            fillHeight = true
                        )
                    }
                }
                repeat(COLUMNS - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
internal fun SimulationNotice() {
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

@Composable
private fun PrimaryReading(
    reading: MetricReading?,
    engine: EngineProfile,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
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
                large = !compact
            )
            RpmBar(fraction = reading.rpmFraction(engine), engine = engine)
        }
    }
}

private fun MetricReading?.rpmFraction(engine: EngineProfile): Float {
    if (this == null || freshness == Freshness.ABSENT) return 0f
    return (sample.value / engine.scaleMaxRpm).toFloat()
}

@Composable
private fun MetricGrid(state: LiveTelemetry, metrics: List<TelemetryMetric>) {
    if (metrics.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(MotoScopeSpacing.small)) {
        metrics.chunked(COLUMNS).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MotoScopeSpacing.small)
            ) {
                row.forEach { metric ->
                    MetricField(
                        metric = metric,
                        reading = state.reading(metric),
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(COLUMNS - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MetricField(
    metric: TelemetryMetric,
    reading: MetricReading?,
    modifier: Modifier = Modifier,
    fillHeight: Boolean = false
) {
    val labels = metric.labels()
    Field(label = labels.name, modifier = modifier, fillHeight = fillHeight) {
        FieldValue(
            value = reading.display(),
            unit = if (reading?.freshness == Freshness.ABSENT) {
                null
            } else {
                stringResource(labels.unit)
            },
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

private const val COLUMNS = 3
