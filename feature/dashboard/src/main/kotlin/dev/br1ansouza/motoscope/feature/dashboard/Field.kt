package dev.br1ansouza.motoscope.feature.dashboard

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopePalette
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeSizes
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeSpacing

@Composable
internal fun Field(
    @StringRes label: Int,
    modifier: Modifier = Modifier,
    fillHeight: Boolean = false,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .border(MotoScopeSizes.fieldBorder, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MotoScopePalette.graphiteRaised)
                .padding(
                    horizontal = MotoScopeSpacing.small,
                    vertical = MotoScopeSpacing.tiny
                ),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = stringResource(label),
                style = MaterialTheme.typography.labelLarge,
                color = MotoScopePalette.gold
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (fillHeight) Modifier.weight(1f) else Modifier)
                .padding(MotoScopeSpacing.small),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            content()
        }
    }
}

@Composable
internal fun FieldValue(
    value: String,
    unit: String?,
    color: androidx.compose.ui.graphics.Color,
    large: Boolean
) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = value,
            style = if (large) {
                MaterialTheme.typography.displayLarge
            } else {
                MaterialTheme.typography.displaySmall
            },
            color = color,
            textAlign = TextAlign.Center
        )
        if (unit != null) {
            Text(
                text = unit,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    start = MotoScopeSpacing.tiny,
                    bottom = MotoScopeSpacing.tiny
                )
            )
        }
    }
}
