package dev.br1ansouza.motoscope

import dev.br1ansouza.motoscope.core.model.TelemetryMetric
import dev.br1ansouza.motoscope.core.recording.RecordingEngine
import dev.br1ansouza.motoscope.core.settings.DashboardLayout
import dev.br1ansouza.motoscope.core.settings.DashboardPreferencesStore
import dev.br1ansouza.motoscope.core.settings.MetricVisibilityStore
import dev.br1ansouza.motoscope.core.telemetry.LiveTelemetry
import dev.br1ansouza.motoscope.core.telemetry.TelemetryEngine
import dev.br1ansouza.motoscope.core.telemetry.TelemetryHub
import dev.br1ansouza.motoscope.core.vehicle.VehicleProfile
import dev.br1ansouza.motoscope.feature.dashboard.DashboardSettings
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.sample
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
    private val retry = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val startup: StateFlow<StartupState> = retry.flatMapLatest {
        flow<StartupState> {
            emit(StartupState.Loading)
            recording.prepare()
            emitAll(
                combine(
                    metrics.visibleMetrics(),
                    preferences.layout(),
                    preferences.vehicle()
                ) { visible, layout, vehicle ->
                    StartupState.Ready(DashboardSettings(visible, layout, vehicle))
                }
            )
        }.catch { emit(StartupState.Failed) }
    }.stateIn(
        scope,
        SharingStarted.WhileSubscribed(replayExpirationMillis = 0),
        StartupState.Loading
    )

    @OptIn(FlowPreview::class)
    val telemetry: StateFlow<LiveTelemetry> = engine.state()
        .sample(PRESENTATION_INTERVAL_MILLIS)
        .stateIn(
            scope,
            SharingStarted.WhileSubscribed(replayExpirationMillis = 0),
            LiveTelemetry(simulated = hub.isSimulated)
        )

    fun retryStartup() {
        retry.value += 1
    }

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

internal sealed interface StartupState {
    data object Loading : StartupState
    data object Failed : StartupState
    data class Ready(val settings: DashboardSettings) : StartupState
}

private const val PRESENTATION_INTERVAL_MILLIS = 100L
