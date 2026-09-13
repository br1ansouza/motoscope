package dev.br1ansouza.motoscope.core.model

data class TelemetryCapabilities(
    private val states: Map<TelemetryMetric, MetricAvailability> = emptyMap()
) {
    fun availabilityOf(metric: TelemetryMetric): MetricAvailability =
        states[metric] ?: MetricAvailability.UNKNOWN

    fun available(): Set<TelemetryMetric> =
        states.filterValues { it == MetricAvailability.AVAILABLE }.keys

    fun with(metric: TelemetryMetric, availability: MetricAvailability): TelemetryCapabilities =
        TelemetryCapabilities(states + (metric to availability))
}
