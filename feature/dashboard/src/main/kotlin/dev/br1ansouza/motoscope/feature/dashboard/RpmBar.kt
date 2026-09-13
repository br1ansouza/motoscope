package dev.br1ansouza.motoscope.feature.dashboard

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopePalette
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeRpmColors
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeSizes
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeSpacing
import dev.br1ansouza.motoscope.core.vehicle.EngineProfile

@Composable
internal fun RpmBar(fraction: Float, engine: EngineProfile, modifier: Modifier = Modifier) {
    val level = animateFloatAsState(
        targetValue = fraction.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = SWEEP_MILLIS, easing = LinearEasing),
        label = "rpm"
    )
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MotoScopeSpacing.tiny)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(MotoScopeSizes.rpmBarHeight)
                .background(MotoScopePalette.graphiteRaised)
                .border(MotoScopeSizes.fieldBorder, MaterialTheme.colorScheme.outline)
        ) {
            drawRect(
                brush = sweepBrush(level.value, engine),
                size = Size(size.width * level.value, size.height)
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
                text = stringResource(R.string.dashboard_primary_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = MetricFormatting.integer(engine.scaleMaxRpm.toDouble()),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun sweepBrush(level: Float, engine: EngineProfile): Brush = Brush.horizontalGradient(
    colors = listOf(levelColor(0f, engine), levelColor(level, engine))
)

private fun levelColor(level: Float, engine: EngineProfile): Color {
    val rpm = level.coerceIn(0f, 1f) * engine.scaleMaxRpm
    val torque = engine.torquePeakRpm.toFloat()
    val power = engine.powerPeakRpm.toFloat()
    val ceiling = engine.scaleMaxRpm.toFloat()
    return when {
        rpm <= torque -> lerp(MotoScopeRpmColors.calm, MotoScopeRpmColors.gold, rpm / torque)
        rpm <= power -> MotoScopeRpmColors.gold
        else -> lerp(
            MotoScopeRpmColors.gold,
            MotoScopeRpmColors.redline,
            ((rpm - power) / (ceiling - power)).coerceIn(0f, 1f)
        )
    }
}

private const val SWEEP_MILLIS = 90
