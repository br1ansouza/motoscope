package dev.br1ansouza.motoscope.core.settings

import dev.br1ansouza.motoscope.core.model.TelemetryMetric

object DashboardMetrics {
    val PRIMARY: TelemetryMetric = TelemetryMetric.ENGINE_RPM

    val SELECTABLE: List<TelemetryMetric> = TelemetryMetric.entries.filterNot { it == PRIMARY }

    val DEFAULT_VISIBLE: Set<TelemetryMetric> = setOf(
        TelemetryMetric.ENGINE_TEMPERATURE,
        TelemetryMetric.SYSTEM_VOLTAGE,
        TelemetryMetric.THROTTLE_POSITION
    )
}
