package dev.br1ansouza.motoscope.feature.dashboard

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import dev.br1ansouza.motoscope.core.model.Freshness
import dev.br1ansouza.motoscope.core.telemetry.MetricReading
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeReadingColors

internal data class MetricDisplay(val value: String, val unit: String?, val color: Color)

@Composable
internal fun metricDisplay(reading: MetricReading?, @StringRes unit: Int?): MetricDisplay {
    val absentValue = stringResource(R.string.dashboard_absent_value)
    val unitText = unit?.let { stringResource(it) }
    val absent = reading == null || reading.freshness == Freshness.ABSENT
    return MetricDisplay(
        value = if (absent) {
            absentValue
        } else {
            MetricFormatting.format(reading.sample.value, reading.sample.unit)
        },
        unit = if (absent) null else unitText,
        color = reading.freshnessColor()
    )
}

internal fun MetricReading?.freshnessColor(): Color = when (this?.freshness) {
    null -> MotoScopeReadingColors.absent
    Freshness.FRESH -> MotoScopeReadingColors.fresh
    Freshness.DELAYED -> MotoScopeReadingColors.delayed
    Freshness.ABSENT -> MotoScopeReadingColors.absent
}
