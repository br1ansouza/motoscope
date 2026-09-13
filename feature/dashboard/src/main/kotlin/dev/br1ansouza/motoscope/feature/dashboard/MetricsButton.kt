package dev.br1ansouza.motoscope.feature.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopePalette
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeSizes
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeSpacing

@Composable
internal fun MetricsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val label = stringResource(R.string.dashboard_metrics_open)
    Surface(
        modifier = modifier
            .width(MotoScopeSizes.metricsButtonWidth)
            .fillMaxHeight()
            .clickable(enabled = enabled, onClickLabel = label) { onClick() }
            .semantics { contentDescription = label },
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(MotoScopeSizes.fieldBorder, MotoScopePalette.gold)
    ) {
        Box(
            modifier = Modifier.padding(MotoScopeSpacing.small),
            contentAlignment = Alignment.Center
        ) {
            TuningGlyph()
        }
    }
}

@Composable
private fun TuningGlyph() {
    Canvas(modifier = Modifier.size(MotoScopeSizes.metricsGlyph)) {
        val rows = GLYPH_ROWS
        val knobs = GLYPH_KNOBS
        val stroke = size.minDimension * STROKE_RATIO
        rows.forEachIndexed { index, row ->
            val y = size.height * row
            drawLine(
                color = MotoScopePalette.edge,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = stroke,
                cap = StrokeCap.Butt
            )
            val x = size.width * knobs[index]
            drawLine(
                color = MotoScopePalette.gold,
                start = Offset(x - stroke, y),
                end = Offset(x + stroke, y),
                strokeWidth = stroke * KNOB_RATIO,
                cap = StrokeCap.Butt
            )
        }
    }
}

private const val ROW_TOP = 0.25f
private const val ROW_MIDDLE = 0.5f
private const val ROW_BOTTOM = 0.75f
private const val KNOB_TOP = 0.7f
private const val KNOB_MIDDLE = 0.35f
private const val KNOB_BOTTOM = 0.6f

private val GLYPH_ROWS = listOf(ROW_TOP, ROW_MIDDLE, ROW_BOTTOM)
private val GLYPH_KNOBS = listOf(KNOB_TOP, KNOB_MIDDLE, KNOB_BOTTOM)

private const val STROKE_RATIO = 0.08f
private const val KNOB_RATIO = 2.4f
