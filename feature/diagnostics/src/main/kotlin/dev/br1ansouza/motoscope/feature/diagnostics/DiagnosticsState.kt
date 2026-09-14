package dev.br1ansouza.motoscope.feature.diagnostics

import dev.br1ansouza.motoscope.protocol.elm327.DiagnosticsReport

sealed interface DiagnosticsState {
    data object Idle : DiagnosticsState

    data object Running : DiagnosticsState

    data class Ready(val report: DiagnosticsReport) : DiagnosticsState
}

data class DiagnosticsActions(val onRun: () -> Unit, val onBack: () -> Unit)
