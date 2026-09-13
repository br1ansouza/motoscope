package dev.br1ansouza.motoscope.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.br1ansouza.motoscope.core.model.TelemetryMetric
import dev.br1ansouza.motoscope.core.settings.DashboardMetrics
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopePalette
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeSizes
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeSpacing
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeStatusColors

@Composable
internal fun MetricPicker(
    visible: Set<TelemetryMetric>,
    onToggle: (TelemetryMetric, Boolean) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .border(MotoScopeSizes.fieldBorder, MotoScopePalette.gold)
            .padding(MotoScopeSpacing.small),
        verticalArrangement = Arrangement.spacedBy(MotoScopeSpacing.small)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.dashboard_metrics_title),
                style = MaterialTheme.typography.titleMedium,
                color = MotoScopePalette.gold
            )
            Text(
                text = stringResource(R.string.dashboard_metrics_close),
                style = MaterialTheme.typography.labelLarge,
                color = MotoScopePalette.ink,
                modifier = Modifier
                    .border(MotoScopeSizes.fieldBorder, MaterialTheme.colorScheme.outline)
                    .clickable { onClose() }
                    .padding(
                        horizontal = MotoScopeSpacing.small,
                        vertical = MotoScopeSpacing.tiny
                    )
            )
        }
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(MotoScopeSpacing.tiny)
        ) {
            DashboardMetrics.SELECTABLE.forEach { metric ->
                MetricToggle(
                    metric = metric,
                    checked = metric in visible,
                    onToggle = { onToggle(metric, it) }
                )
            }
        }
    }
}

@Composable
private fun MetricToggle(metric: TelemetryMetric, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!checked) }
            .padding(vertical = MotoScopeSpacing.tiny),
        horizontalArrangement = Arrangement.spacedBy(MotoScopeSpacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(MotoScopeSizes.checkBox)
                .background(
                    if (checked) MotoScopePalette.gold else MaterialTheme.colorScheme.surface
                )
                .border(MotoScopeSizes.fieldBorder, MaterialTheme.colorScheme.outline)
        )
        Text(
            text = stringResource(metric.labels().name),
            style = MaterialTheme.typography.labelLarge,
            color = if (checked) MotoScopePalette.ink else MotoScopeStatusColors.disabled
        )
    }
}
