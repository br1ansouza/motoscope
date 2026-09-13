package dev.br1ansouza.motoscope.core.telemetry

import dev.br1ansouza.motoscope.core.model.EcuState
import dev.br1ansouza.motoscope.core.model.Freshness
import dev.br1ansouza.motoscope.core.model.TelemetryMetric
import dev.br1ansouza.motoscope.core.model.TelemetrySample
import dev.br1ansouza.motoscope.core.model.TransportState

data class MetricReading(val sample: TelemetrySample, val freshness: Freshness)

data class LiveTelemetry(
    val transport: TransportState = TransportState.DISCONNECTED,
    val ecu: EcuState = EcuState.UNKNOWN,
    val readings: Map<TelemetryMetric, MetricReading> = emptyMap(),
    val simulated: Boolean = false
) {
    fun reading(metric: TelemetryMetric): MetricReading? = readings[metric]
}
