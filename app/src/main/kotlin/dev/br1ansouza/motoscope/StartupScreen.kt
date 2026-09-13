package dev.br1ansouza.motoscope

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopePalette
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeSpacing

@Composable
internal fun StartupScreen(failed: Boolean, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(MotoScopePalette.graphiteRaised, MotoScopePalette.black)
                )
            )
            .padding(MotoScopeSpacing.large),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.widthIn(max = STARTUP_WIDTH),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MotoScopeSpacing.medium)
        ) {
            Image(
                painter = painterResource(R.drawable.startup_motorcycle),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = STARTUP_IMAGE_HEIGHT)
            )
            Wordmark()
            if (failed) {
                FailureBlock(onRetry = onRetry)
            } else {
                LoadingBlock()
            }
        }
    }
}

@Composable
private fun FailureBlock(onRetry: () -> Unit) {
    Text(
        text = stringResource(R.string.startup_failed),
        style = MaterialTheme.typography.bodyLarge,
        color = MotoScopePalette.ink,
        textAlign = TextAlign.Center
    )
    Button(
        onClick = onRetry,
        shape = MaterialTheme.shapes.large,
        colors = ButtonDefaults.buttonColors(
            containerColor = MotoScopePalette.darkRed,
            contentColor = MotoScopePalette.ink
        )
    ) {
        Text(stringResource(R.string.startup_retry))
    }
}

@Composable
private fun LoadingBlock() {
    LinearProgressIndicator(
        modifier = Modifier.fillMaxWidth(),
        color = MotoScopePalette.gold,
        trackColor = MotoScopePalette.darkRed
    )
    Text(
        text = stringResource(R.string.startup_loading),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun Wordmark() {
    Text(
        text = stringResource(R.string.app_name).uppercase(),
        style = MaterialTheme.typography.displaySmall.copy(
            fontFamily = FontFamily.Serif,
            letterSpacing = WORDMARK_TRACKING
        ),
        color = MotoScopePalette.gold,
        textAlign = TextAlign.Center
    )
}

private val STARTUP_WIDTH = 560.dp
private val STARTUP_IMAGE_HEIGHT = 400.dp
private val WORDMARK_TRACKING = 6.sp
