package dev.br1ansouza.motoscope.feature.dashboard

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopePalette
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeRpmColors
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeSizes
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeSpacing

internal const val RPM_SCALE_MAX = 7_000.0

@Composable
internal fun RpmBar(fraction: Float, modifier: Modifier = Modifier) {
    val target = fraction.coerceIn(0f, 1f)
    val level by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = SWEEP_MILLIS, easing = LinearEasing),
        label = "rpm"
    )
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MotoScopeSpacing.tiny)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(MotoScopeSizes.rpmBarHeight)
                .background(MotoScopePalette.graphiteRaised)
                .border(MotoScopeSizes.fieldBorder, MaterialTheme.colorScheme.outline)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(level)
                    .background(sweepBrush(level))
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.dashboard_rpm_scale_min),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(
                    R.string.dashboard_rpm_scale_percent,
                    (level * PERCENT).toInt()
                ),
                style = MaterialTheme.typography.labelLarge,
                color = levelColor(level)
            )
            Text(
                text = MetricFormatting.integer(RPM_SCALE_MAX),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun sweepBrush(level: Float): Brush = Brush.horizontalGradient(
    colors = listOf(levelColor(0f), levelColor(level))
)

private fun levelColor(level: Float): Color {
    val clamped = level.coerceIn(0f, 1f)
    return if (clamped <= MIDPOINT) {
        lerp(MotoScopeRpmColors.calm, MotoScopeRpmColors.gold, clamped / MIDPOINT)
    } else {
        lerp(
            MotoScopeRpmColors.gold,
            MotoScopeRpmColors.redline,
            (clamped - MIDPOINT) / (1f - MIDPOINT)
        )
    }
}

private const val MIDPOINT = 0.55f
private const val SWEEP_MILLIS = 90
private const val PERCENT = 100
