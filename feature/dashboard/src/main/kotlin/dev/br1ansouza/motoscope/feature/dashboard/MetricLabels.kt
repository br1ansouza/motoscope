package dev.br1ansouza.motoscope.feature.dashboard

import androidx.annotation.StringRes
import dev.br1ansouza.motoscope.core.model.MetricUnit
import dev.br1ansouza.motoscope.core.model.TelemetryMetric

internal data class MetricLabels(@param:StringRes val name: Int, @param:StringRes val unit: Int)

internal fun TelemetryMetric.labels(): MetricLabels = when (this) {
    TelemetryMetric.ENGINE_RPM -> MetricLabels(
        R.string.dashboard_primary_label,
        R.string.dashboard_absent_value
    )

    TelemetryMetric.ENGINE_TEMPERATURE -> MetricLabels(
        R.string.dashboard_engine_temperature,
        R.string.dashboard_unit_celsius
    )

    TelemetryMetric.SYSTEM_VOLTAGE -> MetricLabels(
        R.string.dashboard_system_voltage,
        R.string.dashboard_unit_volt
    )

    TelemetryMetric.THROTTLE_POSITION -> MetricLabels(
        R.string.dashboard_throttle,
        R.string.dashboard_unit_percent
    )

    TelemetryMetric.VEHICLE_SPEED -> MetricLabels(
        R.string.dashboard_vehicle_speed,
        R.string.dashboard_unit_kmh
    )

    TelemetryMetric.CALCULATED_ENGINE_LOAD -> MetricLabels(
        R.string.dashboard_engine_load,
        R.string.dashboard_unit_percent
    )

    TelemetryMetric.INTAKE_MANIFOLD_PRESSURE -> MetricLabels(
        R.string.dashboard_manifold_pressure,
        R.string.dashboard_unit_kpa
    )
}

internal fun MetricUnit.matches(metric: TelemetryMetric): Boolean = metric.labels().unit != 0
