package dev.br1ansouza.motoscope.core.ui

import androidx.annotation.StringRes
import dev.br1ansouza.motoscope.core.model.TelemetryMetric

data class MetricLabels(@param:StringRes val name: Int, @param:StringRes val unit: Int)

fun TelemetryMetric.labels(): MetricLabels = when (this) {
    TelemetryMetric.ENGINE_RPM -> MetricLabels(
        R.string.metric_engine_rpm,
        R.string.metric_unit_rpm
    )

    TelemetryMetric.ENGINE_TEMPERATURE -> MetricLabels(
        R.string.metric_engine_temperature,
        R.string.metric_unit_celsius
    )

    TelemetryMetric.SYSTEM_VOLTAGE -> MetricLabels(
        R.string.metric_system_voltage,
        R.string.metric_unit_volt
    )

    TelemetryMetric.THROTTLE_POSITION -> MetricLabels(
        R.string.metric_throttle_position,
        R.string.metric_unit_percent
    )

    TelemetryMetric.VEHICLE_SPEED -> MetricLabels(
        R.string.metric_vehicle_speed,
        R.string.metric_unit_kmh
    )

    TelemetryMetric.CALCULATED_ENGINE_LOAD -> MetricLabels(
        R.string.metric_engine_load,
        R.string.metric_unit_percent
    )

    TelemetryMetric.INTAKE_MANIFOLD_PRESSURE -> MetricLabels(
        R.string.metric_manifold_pressure,
        R.string.metric_unit_kpa
    )
}
