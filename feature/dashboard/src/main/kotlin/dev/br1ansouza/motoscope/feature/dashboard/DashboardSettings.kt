package dev.br1ansouza.motoscope.feature.dashboard

import dev.br1ansouza.motoscope.core.model.TelemetryMetric
import dev.br1ansouza.motoscope.core.settings.DashboardLayout
import dev.br1ansouza.motoscope.core.settings.DashboardMetrics
import dev.br1ansouza.motoscope.core.vehicle.VehicleCatalog
import dev.br1ansouza.motoscope.core.vehicle.VehicleProfile

data class DashboardSettings(
    val visibleMetrics: Set<TelemetryMetric> = DashboardMetrics.DEFAULT_VISIBLE,
    val layout: DashboardLayout = DashboardLayout.PRIMARY_TOP,
    val vehicle: VehicleProfile = VehicleCatalog.DEFAULT
)
