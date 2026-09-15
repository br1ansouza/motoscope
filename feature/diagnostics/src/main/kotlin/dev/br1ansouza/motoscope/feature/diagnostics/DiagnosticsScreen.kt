package dev.br1ansouza.motoscope.feature.diagnostics

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopePalette
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeSizes
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeSpacing
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeStatusColors
import dev.br1ansouza.motoscope.protocol.elm327.DiagnosticsReport
import dev.br1ansouza.motoscope.protocol.elm327.ElmHandshakeStep
import dev.br1ansouza.motoscope.protocol.elm327.SupportedPidRange

@Composable
fun DiagnosticsScreen(
    state: DiagnosticsState,
    actions: DiagnosticsActions,
    simulated: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.safeDrawingPadding()) {
            DiagnosticsHeader(actions.onBack)
            if (simulated) {
                Notice(R.string.diagnostics_simulated, MotoScopeStatusColors.warning)
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(MotoScopeSpacing.small),
                verticalArrangement = Arrangement.spacedBy(MotoScopeSpacing.small)
            ) {
                item {
                    RunButton(enabled = state != DiagnosticsState.Running, onClick = actions.onRun)
                }
                when (state) {
                    DiagnosticsState.Idle -> item { Notice(R.string.diagnostics_idle) }
                    DiagnosticsState.Running -> item { Notice(R.string.diagnostics_running) }
                    is DiagnosticsState.Ready -> report(state.report)
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.report(report: DiagnosticsReport) {
    item { SectionTitle(R.string.diagnostics_section_handshake) }
    items(report.handshake.steps.size) { index ->
        HandshakeRow(
            step = report.handshake.steps[index],
            failed = !report.handshake.completed &&
                index == report.handshake.steps.lastIndex
        )
    }
    item { SectionTitle(R.string.diagnostics_section_adapter) }
    item { AdapterFacts(report) }
    item { SectionTitle(R.string.diagnostics_section_pids) }
    if (report.ranges.isEmpty()) {
        item { Notice(R.string.diagnostics_no_pids) }
    } else {
        items(report.ranges.size) { index -> RangeRow(report.ranges[index]) }
    }
}

@Composable
private fun DiagnosticsHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MotoScopePalette.graphiteRaised)
            .padding(horizontal = MotoScopeSpacing.small, vertical = MotoScopeSpacing.tiny),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.diagnostics_title),
            style = MaterialTheme.typography.titleMedium,
            color = MotoScopePalette.gold
        )
        OutlinedButton(
            onClick = onBack,
            shape = MaterialTheme.shapes.small,
            border = BorderStroke(MotoScopeSizes.fieldBorder, MaterialTheme.colorScheme.outline)
        ) {
            Text(
                text = stringResource(R.string.diagnostics_back),
                style = MaterialTheme.typography.labelLarge,
                color = MotoScopePalette.ink
            )
        }
    }
}

@Composable
private fun RunButton(enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(
            MotoScopeSizes.fieldBorder,
            if (enabled) MotoScopePalette.gold else MaterialTheme.colorScheme.outline
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = MotoScopePalette.darkRed,
            contentColor = MotoScopePalette.ink
        )
    ) {
        Text(
            text = stringResource(
                if (enabled) R.string.diagnostics_run else R.string.diagnostics_running
            ),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(vertical = MotoScopeSpacing.small)
        )
    }
}

@Composable
private fun SectionTitle(@StringRes title: Int) {
    Text(
        text = stringResource(title),
        style = MaterialTheme.typography.labelLarge,
        color = MotoScopePalette.gold,
        modifier = Modifier.fillMaxWidth()
            .padding(top = MotoScopeSpacing.small)
            .background(MotoScopePalette.graphiteRaised)
            .padding(MotoScopeSpacing.small)
    )
}

@Composable
private fun Notice(@StringRes message: Int, color: androidx.compose.ui.graphics.Color? = null) {
    Text(
        text = stringResource(message),
        style = MaterialTheme.typography.labelLarge,
        color = color ?: MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(MotoScopeSpacing.small)
    )
}

@Composable
private fun HandshakeRow(step: ElmHandshakeStep, failed: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MotoScopePalette.graphite)
            .border(
                MotoScopeSizes.fieldBorder,
                if (failed) MotoScopeStatusColors.failure else MaterialTheme.colorScheme.outline
            )
            .padding(MotoScopeSpacing.small)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = step.command.text,
                style = MaterialTheme.typography.labelLarge,
                color = MotoScopePalette.gold
            )
            Text(
                text = step.exchange.describe(),
                modifier = Modifier.weight(1f).padding(start = MotoScopeSpacing.medium),
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.labelLarge,
                color = MotoScopePalette.ink
            )
        }
        Text(
            text = pluralStringResource(
                R.plurals.diagnostics_attempts,
                step.exchange.attempts,
                step.exchange.attempts
            ),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (failed) {
            Text(
                text = stringResource(R.string.diagnostics_handshake_failed),
                style = MaterialTheme.typography.labelLarge,
                color = MotoScopeStatusColors.failure
            )
        }
    }
}

@Composable
private fun AdapterFacts(report: DiagnosticsReport) {
    val absent = stringResource(R.string.diagnostics_absent)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MotoScopePalette.graphite)
            .border(MotoScopeSizes.fieldBorder, MaterialTheme.colorScheme.outline)
            .padding(MotoScopeSpacing.small),
        verticalArrangement = Arrangement.spacedBy(MotoScopeSpacing.tiny)
    ) {
        Fact(R.string.diagnostics_protocol, report.protocol ?: absent)
        Fact(R.string.diagnostics_voltage, report.adapterVoltage ?: absent)
        Fact(R.string.diagnostics_discarded, report.discardedFrames.toString())
    }
}

@Composable
private fun Fact(@StringRes label: Int, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(label),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            modifier = Modifier.weight(1f).padding(start = MotoScopeSpacing.medium),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.labelLarge,
            color = MotoScopePalette.ink
        )
    }
}

@Composable
private fun RangeRow(range: SupportedPidRange) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MotoScopePalette.graphite)
            .border(MotoScopeSizes.fieldBorder, MaterialTheme.colorScheme.outline)
            .padding(MotoScopeSpacing.small),
        verticalArrangement = Arrangement.spacedBy(MotoScopeSpacing.tiny)
    ) {
        Text(
            text = stringResource(R.string.diagnostics_range, range.base.toHex()),
            style = MaterialTheme.typography.labelLarge,
            color = MotoScopePalette.gold
        )
        Text(
            text = range.pids.joinToString(" ") { it.toHex() },
            style = MaterialTheme.typography.labelLarge,
            color = MotoScopePalette.ink
        )
    }
}
