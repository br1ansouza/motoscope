package dev.br1ansouza.motoscope

import dev.br1ansouza.motoscope.core.model.TelemetryMetric
import dev.br1ansouza.motoscope.core.recording.RecordingEngine
import dev.br1ansouza.motoscope.core.settings.DashboardLayout
import dev.br1ansouza.motoscope.core.settings.DashboardMetrics
import dev.br1ansouza.motoscope.core.settings.DashboardPreferencesStore
import dev.br1ansouza.motoscope.core.settings.MetricVisibilityStore
import dev.br1ansouza.motoscope.core.telemetry.LiveTelemetry
import dev.br1ansouza.motoscope.core.telemetry.TelemetryEngine
import dev.br1ansouza.motoscope.core.telemetry.TelemetryHub
import dev.br1ansouza.motoscope.core.vehicle.VehicleCatalog
import dev.br1ansouza.motoscope.core.vehicle.VehicleProfile
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Singleton
internal class TelemetryRuntime @Inject constructor(
    private val hub: TelemetryHub,
    private val engine: TelemetryEngine,
    val recording: RecordingEngine,
    private val metrics: MetricVisibilityStore,
    private val preferences: DashboardPreferencesStore,
    private val scope: CoroutineScope
) {
    val visibleMetrics: StateFlow<Set<TelemetryMetric>> = metrics.visibleMetrics()
        .stateIn(scope, SharingStarted.Eagerly, DashboardMetrics.DEFAULT_VISIBLE)

    val layout: StateFlow<DashboardLayout> = preferences.layout()
        .stateIn(scope, SharingStarted.Eagerly, DashboardLayout.PRIMARY_TOP)

    val vehicle: StateFlow<VehicleProfile> = preferences.vehicle()
        .stateIn(scope, SharingStarted.Eagerly, VehicleCatalog.DEFAULT)
    val telemetry: StateFlow<LiveTelemetry> = engine.state()
        .stateIn(scope, SharingStarted.Eagerly, LiveTelemetry())

    suspend fun setMetricVisible(metric: TelemetryMetric, visible: Boolean) {
        metrics.setVisible(metric, visible)
    }

    suspend fun setLayout(layout: DashboardLayout) {
        preferences.setLayout(layout)
    }

    suspend fun setVehicle(profile: VehicleProfile) {
        preferences.setVehicle(profile)
    }

    fun start() {
        scope.launch { recording.run() }
        scope.launch { hub.run() }
    }
}
