package dev.br1ansouza.motoscope

import dev.br1ansouza.motoscope.core.recording.RecordingEngine
import dev.br1ansouza.motoscope.core.telemetry.LiveTelemetry
import dev.br1ansouza.motoscope.core.telemetry.TelemetryEngine
import dev.br1ansouza.motoscope.core.telemetry.TelemetryHub
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Singleton
internal class TelemetryRuntime @Inject constructor(
    private val hub: TelemetryHub,
    private val engine: TelemetryEngine,
    val recording: RecordingEngine,
    private val scope: CoroutineScope
) {
    val telemetry: StateFlow<LiveTelemetry> = engine.state()
        .stateIn(scope, SharingStarted.Eagerly, LiveTelemetry())

    fun start() {
        scope.launch { recording.run() }
        scope.launch { hub.run() }
    }
}
