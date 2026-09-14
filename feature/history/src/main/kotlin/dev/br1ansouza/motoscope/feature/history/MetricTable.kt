package dev.br1ansouza.motoscope.feature.history

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import dev.br1ansouza.motoscope.core.model.MetricSummary
import dev.br1ansouza.motoscope.core.ui.labels
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopePalette
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeSizes
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeSpacing

@Composable
internal fun MetricTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MotoScopePalette.graphiteRaised)
            .padding(MotoScopeSpacing.tiny),
        horizontalArrangement = Arrangement.spacedBy(MotoScopeSpacing.tiny)
    ) {
        HeaderCell(R.string.history_column_metric, WIDE_COLUMN, TextAlign.Start)
        HeaderCell(R.string.history_column_minimum, NARROW_COLUMN, TextAlign.End)
        HeaderCell(R.string.history_column_maximum, NARROW_COLUMN, TextAlign.End)
        HeaderCell(R.string.history_column_average, NARROW_COLUMN, TextAlign.End)
    }
}

@Composable
private fun RowScope.HeaderCell(@StringRes label: Int, weight: Float, align: TextAlign) {
    Text(
        text = stringResource(label),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        textAlign = align,
        modifier = Modifier.weight(weight)
    )
}

@Composable
internal fun MetricRow(summary: MetricSummary) {
    val labels = summary.metric.labels()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(MotoScopeSizes.fieldBorder, MaterialTheme.colorScheme.outline)
            .padding(MotoScopeSpacing.tiny),
        horizontalArrangement = Arrangement.spacedBy(MotoScopeSpacing.tiny)
    ) {
        Text(
            text = stringResource(
                R.string.history_metric_with_unit,
                stringResource(labels.name),
                stringResource(labels.unit)
            ),
            style = MaterialTheme.typography.labelLarge,
            color = MotoScopePalette.gold,
            modifier = Modifier.weight(WIDE_COLUMN)
        )
        listOf(summary.minimum, summary.maximum, summary.average).forEach { value ->
            Text(
                text = HistoryFormatting.value(value, summary.unit),
                style = MaterialTheme.typography.labelLarge,
                color = MotoScopePalette.ink,
                maxLines = 1,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(NARROW_COLUMN)
            )
        }
    }
}

internal const val WIDE_COLUMN = 2f
internal const val NARROW_COLUMN = 1f
