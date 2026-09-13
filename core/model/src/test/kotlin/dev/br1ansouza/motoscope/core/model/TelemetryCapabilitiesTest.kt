package dev.br1ansouza.motoscope.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class TelemetryCapabilitiesTest {
    @Test
    fun untestedMetricIsUnknownInsteadOfAvailable() {
        val capabilities = TelemetryCapabilities()

        TelemetryMetric.entries.forEach { metric ->
            assertEquals(MetricAvailability.UNKNOWN, capabilities.availabilityOf(metric))
        }
        assertEquals(emptySet<TelemetryMetric>(), capabilities.available())
    }

    @Test
    fun discoveryResultIsRecordedPerMetric() {
        val capabilities = TelemetryCapabilities()
            .with(TelemetryMetric.ENGINE_RPM, MetricAvailability.AVAILABLE)
            .with(TelemetryMetric.INTAKE_MANIFOLD_PRESSURE, MetricAvailability.UNAVAILABLE)

        assertEquals(
            MetricAvailability.AVAILABLE,
            capabilities.availabilityOf(TelemetryMetric.ENGINE_RPM)
        )
        assertEquals(
            MetricAvailability.UNAVAILABLE,
            capabilities.availabilityOf(TelemetryMetric.INTAKE_MANIFOLD_PRESSURE)
        )
        assertEquals(setOf(TelemetryMetric.ENGINE_RPM), capabilities.available())
    }

    @Test
    fun laterDiscoveryReplacesTheEarlierState() {
        val capabilities = TelemetryCapabilities()
            .with(TelemetryMetric.VEHICLE_SPEED, MetricAvailability.AVAILABLE)
            .with(TelemetryMetric.VEHICLE_SPEED, MetricAvailability.UNAVAILABLE)

        assertEquals(
            MetricAvailability.UNAVAILABLE,
            capabilities.availabilityOf(TelemetryMetric.VEHICLE_SPEED)
        )
        assertEquals(emptySet<TelemetryMetric>(), capabilities.available())
    }

    @Test
    fun recordingAMetricDoesNotChangeTheOriginalValue() {
        val discovered = TelemetryCapabilities()
        val updated = discovered.with(TelemetryMetric.ENGINE_RPM, MetricAvailability.AVAILABLE)

        assertEquals(
            MetricAvailability.UNKNOWN,
            discovered.availabilityOf(TelemetryMetric.ENGINE_RPM)
        )
        assertEquals(setOf(TelemetryMetric.ENGINE_RPM), updated.available())
    }
}
