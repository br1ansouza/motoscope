package dev.br1ansouza.motoscope.feature.dashboard

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
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
        verticalArrangement = Arrangement.spacedBy(MotoScopeSpacing.small)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(MotoScopeSizes.rpmBarHeight)
                .rpmSegments(level, engine)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            repeat(SCALE_DIVISIONS + 1) { index ->
                Text(
                    text = if (index == 0) {
                        stringResource(R.string.dashboard_rpm_scale_min)
                    } else {
                        MetricFormatting.integer(engine.scaleMaxRpm.toDouble() * index / SCALE_DIVISIONS)
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun Modifier.rpmSegments(level: State<Float>, engine: EngineProfile): Modifier = drawWithCache {
    val step = size.width / SEGMENTS
    val gap = MotoScopeSizes.rpmSegmentGap.toPx().coerceAtMost(step * MAX_GAP_RATIO)
    val radius = CornerRadius(MotoScopeSizes.rpmSegmentRadius.toPx())
    val segments = List(SEGMENTS) { index ->
        val position = index.toFloat() / (SEGMENTS - 1)
        val height = size.height * (MIN_HEIGHT + HEIGHT_RISE * position)
        RpmSegment(
            topLeft = Offset(index * step, size.height - height),
            size = Size(step - gap, height),
            color = segmentColor(position, engine)
        )
    }
    onDrawBehind {
        segments.forEach { segment ->
            drawRoundRect(
                color = segment.color.copy(alpha = INACTIVE_ALPHA),
                topLeft = segment.topLeft,
                size = segment.size,
                cornerRadius = radius
            )
        }
        clipRect(right = size.width * level.value) {
            segments.forEach { segment ->
                drawRoundRect(
                    color = segment.color,
                    topLeft = segment.topLeft,
                    size = segment.size,
                    cornerRadius = radius
                )
            }
        }
    }
}

private data class RpmSegment(val topLeft: Offset, val size: Size, val color: Color)

private fun segmentColor(position: Float, engine: EngineProfile): Color {
    val rpm = position * engine.scaleMaxRpm
    val torque = engine.torquePeakRpm.toFloat()
    val power = engine.powerPeakRpm.toFloat()
    return when {
        rpm <= torque -> lerp(MotoScopeRpmColors.calm, MotoScopeRpmColors.gold, rpm / torque)
        rpm <= power -> MotoScopeRpmColors.gold
        else -> lerp(
            MotoScopeRpmColors.gold,
            MotoScopeRpmColors.redline,
            ((rpm - power) / (engine.scaleMaxRpm - power)).coerceIn(0f, 1f)
        )
    }
}

private const val SEGMENTS = 48
private const val SCALE_DIVISIONS = 7
private const val SWEEP_MILLIS = 90
private const val MIN_HEIGHT = 0.45f
private const val HEIGHT_RISE = 0.55f
private const val MAX_GAP_RATIO = 0.3f
private const val INACTIVE_ALPHA = 0.13f
