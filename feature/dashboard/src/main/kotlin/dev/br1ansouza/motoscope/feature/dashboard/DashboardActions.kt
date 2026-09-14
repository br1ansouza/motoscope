package dev.br1ansouza.motoscope.feature.dashboard

import dev.br1ansouza.motoscope.core.model.TelemetryMetric
import dev.br1ansouza.motoscope.core.settings.DashboardLayout
import dev.br1ansouza.motoscope.core.vehicle.VehicleProfile

data class DashboardActions(
    val onToggleMetric: (TelemetryMetric, Boolean) -> Unit,
    val onSelectLayout: (DashboardLayout) -> Unit,
    val onSelectVehicle: (VehicleProfile) -> Unit,
    val onStartRecording: () -> Unit,
    val onStopRecording: () -> Unit,
    val onOpenHistory: () -> Unit
)
