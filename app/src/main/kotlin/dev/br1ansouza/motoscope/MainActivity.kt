package dev.br1ansouza.motoscope

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import dagger.hilt.android.AndroidEntryPoint
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeTheme
import dev.br1ansouza.motoscope.feature.dashboard.DashboardActions
import dev.br1ansouza.motoscope.feature.dashboard.DashboardScreen
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    internal lateinit var runtime: TelemetryRuntime

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            MotoScopeTheme {
                val state by runtime.telemetry.collectAsState()
                val recording by runtime.recording.state.collectAsState()
                val metrics by runtime.visibleMetrics.collectAsState()
                val scope = rememberCoroutineScope()
                DashboardScreen(
                    state = state,
                    recording = recording,
                    visibleMetrics = metrics,
                    actions = DashboardActions(
                        onToggleMetric = { metric, visible ->
                            scope.launch { runtime.setMetricVisible(metric, visible) }
                        },
                        onStartRecording = { scope.launch { runtime.recording.start() } },
                        onStopRecording = { scope.launch { runtime.recording.stop() } }
                    )
                )
            }
        }
    }
}
