package dev.br1ansouza.motoscope

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import dev.br1ansouza.motoscope.core.model.SessionId
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeTheme
import dev.br1ansouza.motoscope.feature.dashboard.DashboardActions
import dev.br1ansouza.motoscope.feature.dashboard.DashboardScreen
import dev.br1ansouza.motoscope.feature.history.HistoryActions
import dev.br1ansouza.motoscope.feature.history.HistoryScreen
import dev.br1ansouza.motoscope.feature.history.SessionDetailScreen
import dev.br1ansouza.motoscope.feature.history.SessionDetailState
import dev.br1ansouza.motoscope.feature.recording.RecordingService
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    internal lateinit var runtime: TelemetryRuntime

    @Inject
    internal lateinit var history: HistoryRuntime

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
                var historyOpen by rememberSaveable { mutableStateOf(false) }
                var openSessionId by rememberSaveable { mutableStateOf<String?>(null) }
                if (historyOpen) {
                    HistoryRoute(
                        openSessionId = openSessionId,
                        onOpenSession = { openSessionId = it.value },
                        onCloseSession = { openSessionId = null },
                        onCloseHistory = {
                            openSessionId = null
                            historyOpen = false
                        }
                    )
                    return@MotoScopeTheme
                }
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
                        onStopRecording = { RecordingService.stop(this@MainActivity) },
                        onOpenHistory = { historyOpen = true }
                    )
                )
            }
        }
    }

    @Composable
    private fun HistoryRoute(
        openSessionId: String?,
        onOpenSession: (SessionId) -> Unit,
        onCloseSession: () -> Unit,
        onCloseHistory: () -> Unit
    ) {
        val scope = rememberCoroutineScope()
        if (openSessionId == null) {
            BackHandler(onBack = onCloseHistory)
            val sessions by history.sessions.collectAsStateWithLifecycle()
            HistoryScreen(
                state = sessions,
                actions = HistoryActions(
                    onOpenSession = onOpenSession,
                    onDeleteSession = {},
                    onBack = onCloseHistory
                )
            )
            return
        }
        BackHandler(onBack = onCloseSession)
        val id = SessionId(openSessionId)
        var detail by remember(openSessionId) {
            mutableStateOf<SessionDetailState>(SessionDetailState.Loading)
        }
        LaunchedEffect(openSessionId) { detail = history.detail(id) }
        SessionDetailScreen(
            state = detail,
            actions = HistoryActions(
                onOpenSession = onOpenSession,
                onDeleteSession = {
                    scope.launch {
                        history.delete(it)
                        onCloseSession()
                    }
                },
                onBack = onCloseSession
            )
        )
    }
}
