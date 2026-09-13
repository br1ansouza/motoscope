package dev.br1ansouza.motoscope

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeTheme
import dev.br1ansouza.motoscope.feature.dashboard.DashboardActions
import dev.br1ansouza.motoscope.feature.dashboard.DashboardScreen
import dev.br1ansouza.motoscope.feature.recording.RecordingService
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    internal lateinit var runtime: TelemetryRuntime

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            MotoScopeTheme {
                val startup by runtime.startup.collectAsStateWithLifecycle()
                val ready = startup as? StartupState.Ready
                if (ready == null) {
                    StartupScreen(startup == StartupState.Failed, runtime::retryStartup)
                    return@MotoScopeTheme
                }
                val state by runtime.telemetry.collectAsStateWithLifecycle()
                val recording by runtime.recording.state.collectAsStateWithLifecycle()
                val scope = rememberCoroutineScope()
                DashboardScreen(
                    state = state,
                    recording = recording,
                    settings = ready.settings,
                    actions = DashboardActions(
                        onToggleMetric = { metric, visible ->
                            scope.launch { runtime.setMetricVisible(metric, visible) }
                        },
                        onSelectLayout = { scope.launch { runtime.setLayout(it) } },
                        onSelectVehicle = { scope.launch { runtime.setVehicle(it) } },
                        onStartRecording = {
                            requestNotificationPermission()
                            RecordingService.start(this@MainActivity)
                        },
                        onStopRecording = { RecordingService.stop(this@MainActivity) }
                    )
                )
            }
        }
    }
}
