package dev.br1ansouza.motoscope.core.telemetry

import dev.br1ansouza.motoscope.core.model.EcuState
import dev.br1ansouza.motoscope.core.model.TelemetrySample
import dev.br1ansouza.motoscope.core.model.TransportState

sealed interface TelemetryEvent {
    data class TransportChanged(val state: TransportState) : TelemetryEvent

    data class EcuChanged(val state: EcuState) : TelemetryEvent

    data class SampleReceived(val sample: TelemetrySample) : TelemetryEvent
}
