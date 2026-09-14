package dev.br1ansouza.motoscope.feature.dashboard

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import dev.br1ansouza.motoscope.core.model.TelemetryMetric
import dev.br1ansouza.motoscope.core.settings.DashboardLayout
import dev.br1ansouza.motoscope.core.settings.DashboardMetrics
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopePalette
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeSizes
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeSpacing
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeStatusColors
import dev.br1ansouza.motoscope.core.vehicle.VehicleCatalog
import dev.br1ansouza.motoscope.core.vehicle.VehicleProfile

@Composable
internal fun ConfigurationPanel(
    settings: DashboardSettings,
    actions: DashboardActions,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(MotoScopeSizes.sidebarWidth)
            .fillMaxHeight()
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
                text = stringResource(R.string.dashboard_menu_title),
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
        ConfigurationSections(settings = settings, actions = actions)
    }
}

@Composable
private fun ConfigurationSections(settings: DashboardSettings, actions: DashboardActions) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(MotoScopeSpacing.small)
    ) {
        Section(R.string.dashboard_section_metrics, expandedByDefault = true) {
            DashboardMetrics.SELECTABLE.forEach { metric ->
                ToggleRow(
                    metric = metric,
                    checked = metric in settings.visibleMetrics,
                    onToggle = { actions.onToggleMetric(metric, it) }
                )
            }
        }
        Section(R.string.dashboard_section_layout, expandedByDefault = true) {
            LayoutChooser(
                selected = settings.layout,
                onSelect = actions.onSelectLayout
            )
        }
        Section(R.string.dashboard_section_vehicle, expandedByDefault = false) {
            VehicleCatalog.ALL.forEach { profile ->
                ChoiceRow(
                    label = profile.displayName,
                    detail = profile.engineSummary(),
                    selected = profile.id == settings.vehicle.id,
                    onClick = { actions.onSelectVehicle(profile) }
                )
            }
        }
        Section(R.string.dashboard_section_storage, expandedByDefault = false) {
            Text(
                text = stringResource(R.string.dashboard_storage_pending),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = MotoScopeSpacing.tiny)
            )
            Text(
                text = stringResource(R.string.dashboard_open_history),
                style = MaterialTheme.typography.labelLarge,
                color = MotoScopePalette.gold,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(MotoScopeSizes.fieldBorder, MaterialTheme.colorScheme.outline)
                    .clickable { actions.onOpenHistory() }
                    .padding(MotoScopeSpacing.small)
            )
        }
    }
}

@Composable
private fun Section(
    @StringRes title: Int,
    expandedByDefault: Boolean,
    content: @Composable () -> Unit
) {
    var expanded by rememberSaveable(title) { mutableStateOf(expandedByDefault) }
    Column(verticalArrangement = Arrangement.spacedBy(MotoScopeSpacing.tiny)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MotoScopePalette.graphiteRaised)
                .clickable { expanded = !expanded }
                .padding(MotoScopeSpacing.tiny),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(title),
                style = MaterialTheme.typography.labelLarge,
                color = MotoScopePalette.gold
            )
            Text(
                text = stringResource(
                    if (expanded) {
                        R.string.dashboard_section_collapse
                    } else {
                        R.string.dashboard_section_expand
                    }
                ),
                style = MaterialTheme.typography.labelLarge,
                color = MotoScopePalette.gold
            )
        }
        if (expanded) {
            content()
        }
    }
}

@Composable
private fun LayoutChooser(selected: DashboardLayout, onSelect: (DashboardLayout) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MotoScopeSpacing.small)
    ) {
        DashboardLayout.entries.forEach { layout ->
            val label = stringResource(layout.labelRes())
            Image(
                painter = painterResource(layout.iconRes()),
                contentDescription = label,
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .border(
                        width = MotoScopeSizes.fieldBorder,
                        color = if (layout == selected) {
                            MotoScopePalette.gold
                        } else {
                            MaterialTheme.colorScheme.outline
                        }
                    )
                    .clickable(onClickLabel = label) { onSelect(layout) }
                    .padding(MotoScopeSpacing.tiny)
                    .alpha(if (layout == selected) 1f else UNSELECTED_ALPHA)
            )
        }
    }
}

private fun DashboardLayout.iconRes(): Int = when (this) {
    DashboardLayout.PRIMARY_TOP -> R.drawable.ic_layout_primary_top
    DashboardLayout.PRIMARY_CENTERED -> R.drawable.ic_layout_primary_centered
    DashboardLayout.UNIFORM -> R.drawable.ic_layout_uniform
}

private const val UNSELECTED_ALPHA = 0.45f

@Composable
private fun ChoiceRow(label: String, detail: String?, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = MotoScopeSpacing.tiny),
        horizontalArrangement = Arrangement.spacedBy(MotoScopeSpacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Marker(filled = selected)
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) MotoScopePalette.ink else MotoScopeStatusColors.disabled
            )
            if (detail != null) {
                Text(
                    text = detail,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ToggleRow(metric: TelemetryMetric, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!checked) }
            .padding(vertical = MotoScopeSpacing.tiny),
        horizontalArrangement = Arrangement.spacedBy(MotoScopeSpacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Marker(filled = checked)
        Text(
            text = stringResource(metric.labels().name),
            style = MaterialTheme.typography.labelLarge,
            color = if (checked) MotoScopePalette.ink else MotoScopeStatusColors.disabled
        )
    }
}

@Composable
private fun Marker(filled: Boolean) {
    Box(
        modifier = Modifier
            .size(MotoScopeSizes.checkBox)
            .background(if (filled) MotoScopePalette.gold else MaterialTheme.colorScheme.surface)
            .border(MotoScopeSizes.fieldBorder, MaterialTheme.colorScheme.outline)
    )
}

@Composable
private fun VehicleProfile.engineSummary(): String = stringResource(
    R.string.dashboard_vehicle_engine,
    engine.displacementCc,
    MetricFormatting.integer(engine.revLimitRpm.toDouble())
)

private fun DashboardLayout.labelRes(): Int = when (this) {
    DashboardLayout.PRIMARY_TOP -> R.string.dashboard_layout_primary_top
    DashboardLayout.PRIMARY_CENTERED -> R.string.dashboard_layout_primary_centered
    DashboardLayout.UNIFORM -> R.string.dashboard_layout_uniform
}
