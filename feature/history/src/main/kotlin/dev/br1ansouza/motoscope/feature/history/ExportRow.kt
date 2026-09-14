package dev.br1ansouza.motoscope.feature.history

import androidx.annotation.StringRes
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import dev.br1ansouza.motoscope.core.history.ExportFormat
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopePalette
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeSizes
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeSpacing

@Composable
internal fun ExportRow(enabled: Boolean, onExport: (ExportFormat) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MotoScopeSpacing.small)
    ) {
        ExportButton(
            label = R.string.history_export_csv,
            enabled = enabled,
            onClick = { onExport(ExportFormat.CSV) },
            modifier = Modifier.weight(1f)
        )
        ExportButton(
            label = R.string.history_export_json,
            enabled = enabled,
            onClick = { onExport(ExportFormat.JSON) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ExportButton(
    @StringRes label: Int,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(label),
        style = MaterialTheme.typography.labelLarge,
        color = if (enabled) MotoScopePalette.gold else MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = modifier
            .border(MotoScopeSizes.fieldBorder, MaterialTheme.colorScheme.outline)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(MotoScopeSpacing.small)
    )
}

