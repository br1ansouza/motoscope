package dev.br1ansouza.motoscope.feature.dashboard

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopePalette
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeSizes
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeSpacing

@Composable
internal fun StorageAndDiagnosticsSections(actions: DashboardActions) {
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
    Section(R.string.dashboard_section_diagnostics, expandedByDefault = false) {
        Text(
            text = stringResource(R.string.dashboard_diagnostics_hint),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = MotoScopeSpacing.tiny)
        )
        Text(
            text = stringResource(R.string.dashboard_open_diagnostics),
            style = MaterialTheme.typography.labelLarge,
            color = MotoScopePalette.gold,
            modifier = Modifier
                .fillMaxWidth()
                .border(MotoScopeSizes.fieldBorder, MaterialTheme.colorScheme.outline)
                .clickable { actions.onOpenDiagnostics() }
                .padding(MotoScopeSpacing.small)
        )
    }
}

