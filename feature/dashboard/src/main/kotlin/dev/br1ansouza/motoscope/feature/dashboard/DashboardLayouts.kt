package dev.br1ansouza.motoscope.feature.dashboard

import androidx.annotation.StringRes
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.br1ansouza.motoscope.core.model.Freshness
import dev.br1ansouza.motoscope.core.model.TelemetryMetric
import dev.br1ansouza.motoscope.core.settings.DashboardLayout
import dev.br1ansouza.motoscope.core.settings.DashboardMetrics
import dev.br1ansouza.motoscope.core.telemetry.LiveTelemetry
import dev.br1ansouza.motoscope.core.telemetry.MetricReading
import dev.br1ansouza.motoscope.core.ui.labels
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
    val ordered = remember(settings.visibleMetrics) {
        DashboardMetrics.SELECTABLE.filter { it in settings.visibleMetrics }
    }
    val engine = settings.vehicle.engine
    val rpm = state.reading(TelemetryMetric.ENGINE_RPM)
    val primary = metricDisplay(rpm, null)
    val fraction = rpm.rpmFraction(engine)
    val shortScreen = windowHeight() < SHORT_SCREEN_HEIGHT
    Box(modifier = modifier) {
        if (shortScreen && settings.layout != DashboardLayout.UNIFORM) {
            Row(horizontalArrangement = Arrangement.spacedBy(MotoScopeSpacing.small)) {
                PrimaryReading(
                    display = primary,
                    fraction = fraction,
                    engine = engine,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    compact = true
                )
                if (ordered.isNotEmpty()) {
                    Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                        MetricGrid(state = state, metrics = ordered, columns = 2)
                    }
                }
            }
        } else {
            StandardDashboardBody(
                state = state,
                settings = settings,
                ordered = ordered,
                primary = PrimaryDisplay(primary, fraction, engine),
                modifier = Modifier.fillMaxWidth().fillMaxHeight()
            )
        }
    }
}

internal data class PrimaryDisplay(
    val display: MetricDisplay,
    val fraction: Float,
    val engine: EngineProfile
)

@Composable
private fun StandardDashboardBody(
    state: LiveTelemetry,
    settings: DashboardSettings,
    ordered: List<TelemetryMetric>,
    primary: PrimaryDisplay,
    modifier: Modifier = Modifier
) {
    when (settings.layout) {
        DashboardLayout.PRIMARY_TOP -> Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(MotoScopeSpacing.small)
        ) {
            PrimaryReading(
                display = primary.display,
                fraction = primary.fraction,
                engine = primary.engine,
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
                display = primary.display,
                fraction = primary.fraction,
                engine = primary.engine,
                modifier = Modifier.weight(1f)
            )
            MetricGrid(state = state, metrics = ordered.drop(half))
        }

        DashboardLayout.UNIFORM -> UniformGrid(
            state = state,
            metrics = ordered,
            primary = primary,
            modifier = modifier
        )
    }
}

@Composable
private fun UniformGrid(
    state: LiveTelemetry,
    metrics: List<TelemetryMetric>,
    primary: PrimaryDisplay,
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
                            display = primary.display,
                            fraction = primary.fraction,
                            engine = primary.engine,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            compact = true
                        )
                    } else {
                        val labels = metric.labels()
                        MetricField(
                            label = labels.name,
                            display = metricDisplay(state.reading(metric), labels.unit),
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
    display: MetricDisplay,
    fraction: Float,
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
            FieldValue(display = display, large = !compact)
            if (!compact) {
                RpmBar(fraction = fraction, engine = engine)
            }
        }
    }
}

private fun MetricReading?.rpmFraction(engine: EngineProfile): Float {
    if (this == null || freshness == Freshness.ABSENT) return 0f
    return (sample.value / engine.scaleMaxRpm).toFloat()
}

@Composable
private fun MetricGrid(
    state: LiveTelemetry,
    metrics: List<TelemetryMetric>,
    columns: Int = COLUMNS
) {
    if (metrics.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(MotoScopeSpacing.small)) {
        metrics.chunked(columns).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MotoScopeSpacing.small)
            ) {
                row.forEach { metric ->
                    val labels = metric.labels()
                    MetricField(
                        label = labels.name,
                        display = metricDisplay(state.reading(metric), labels.unit),
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(columns - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MetricField(
    @StringRes label: Int,
    display: MetricDisplay,
    modifier: Modifier = Modifier,
    fillHeight: Boolean = false
) {
    Field(label = label, modifier = modifier, fillHeight = fillHeight) {
        FieldValue(display = display, large = false)
    }
}

private const val COLUMNS = 3

private val SHORT_SCREEN_HEIGHT = 300.dp
