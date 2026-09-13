package dev.br1ansouza.motoscope.feature.dashboard

import dev.br1ansouza.motoscope.core.model.TelemetryMetric

data class DashboardActions(
    val onToggleMetric: (TelemetryMetric, Boolean) -> Unit,
    val onStartRecording: () -> Unit,
    val onStopRecording: () -> Unit
)
