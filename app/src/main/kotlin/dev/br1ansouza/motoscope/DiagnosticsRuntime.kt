package dev.br1ansouza.motoscope

import dev.br1ansouza.motoscope.feature.diagnostics.DiagnosticsState
import dev.br1ansouza.motoscope.protocol.elm327.DiagnosticsProbe
import dev.br1ansouza.motoscope.protocol.elm327.ElmCommandQueue
import dev.br1ansouza.motoscope.protocol.elm327.ElmTransport
import dev.br1ansouza.motoscope.simulator.SimulatedElmTransport
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Singleton
internal class DiagnosticsRuntime @Inject constructor(
    queue: ElmCommandQueue,
    transport: ElmTransport,
    private val scope: CoroutineScope
) {
    private val probe = DiagnosticsProbe(queue)

    private val current = MutableStateFlow<DiagnosticsState>(DiagnosticsState.Idle)

    val state: StateFlow<DiagnosticsState> = current.asStateFlow()

    val simulated: Boolean = transport is SimulatedElmTransport

    fun run() {
        if (current.value == DiagnosticsState.Running) return
        current.value = DiagnosticsState.Running
        scope.launch {
            current.value = DiagnosticsState.Ready(probe.run())
        }
    }
}
