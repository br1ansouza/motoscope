package dev.br1ansouza.motoscope

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopePalette
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeSpacing

@Composable
internal fun StartupScreen(failed: Boolean, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(MotoScopePalette.graphiteRaised, MotoScopePalette.black))
        ).padding(MotoScopeSpacing.large),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.widthIn(max = STARTUP_WIDTH),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MotoScopeSpacing.medium)
        ) {
            Image(
                painter = painterResource(R.drawable.ic_motoscope),
                contentDescription = null,
                modifier = Modifier.size(STARTUP_LOGO_SIZE)
            )
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displaySmall,
                color = MotoScopePalette.ink
            )
            Text(
                text = stringResource(R.string.startup_tagline),
                style = MaterialTheme.typography.labelLarge,
                color = MotoScopePalette.gold,
                textAlign = TextAlign.Center
            )
            if (failed) {
                Text(
                    stringResource(R.string.startup_failed),
                    color = MotoScopePalette.ink,
                    textAlign = TextAlign.Center
                )
                Button(onClick = onRetry) { Text(stringResource(R.string.startup_retry)) }
            } else {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MotoScopePalette.gold,
                    trackColor = MotoScopePalette.darkRed
                )
                Text(
                    stringResource(R.string.startup_loading),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private val STARTUP_WIDTH = 320.dp
private val STARTUP_LOGO_SIZE = 104.dp
