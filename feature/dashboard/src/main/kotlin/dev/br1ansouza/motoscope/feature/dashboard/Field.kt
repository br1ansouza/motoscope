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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
internal fun FieldValue(display: MetricDisplay, large: Boolean) {
    val narrow = windowWidth() < NARROW_VALUE_WIDTH
    val fullStyle = if (large) {
        MaterialTheme.typography.displayLarge
    } else {
        MaterialTheme.typography.displaySmall
    }
    val baseStyle = remember(fullStyle, narrow) {
        if (narrow) {
            fullStyle.copy(fontSize = fullStyle.fontSize * NARROW_VALUE_SCALE)
        } else {
            fullStyle
        }
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = display.value,
            style = baseStyle,
            color = display.color,
            maxLines = 1,
            softWrap = false,
            textAlign = TextAlign.Center
        )
        if (display.unit != null) {
            Text(
                text = display.unit,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = MotoScopeSpacing.tiny)
            )
        }
    }
}

private val NARROW_VALUE_WIDTH = 600.dp
private const val NARROW_VALUE_SCALE = 0.6f
