package dev.br1ansouza.motoscope

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
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
import dev.br1ansouza.motoscope.core.history.ExportFormat
import dev.br1ansouza.motoscope.core.model.SessionId
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeTheme
import dev.br1ansouza.motoscope.feature.dashboard.DashboardActions
import dev.br1ansouza.motoscope.feature.dashboard.DashboardScreen
import dev.br1ansouza.motoscope.feature.diagnostics.DiagnosticsActions
import dev.br1ansouza.motoscope.feature.diagnostics.DiagnosticsScreen
import dev.br1ansouza.motoscope.feature.history.ExportNotice
import dev.br1ansouza.motoscope.feature.history.HistoryActions
import dev.br1ansouza.motoscope.feature.history.HistoryScreen
import dev.br1ansouza.motoscope.feature.history.SessionDetailScreen
import dev.br1ansouza.motoscope.feature.history.SessionDetailState
import dev.br1ansouza.motoscope.feature.recording.RecordingService
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    internal lateinit var runtime: TelemetryRuntime

    @Inject
    internal lateinit var history: HistoryRuntime

    @Inject
    internal lateinit var diagnostics: DiagnosticsRuntime

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
                var historyOpen by rememberSaveable { mutableStateOf(false) }
                var diagnosticsOpen by rememberSaveable { mutableStateOf(false) }
                var openSessionId by rememberSaveable { mutableStateOf<String?>(null) }
                val startup by runtime.startup.collectAsStateWithLifecycle()
                val ready = startup as? StartupState.Ready
                if (ready == null) {
                    StartupScreen(startup == StartupState.Failed, runtime::retryStartup)
                    return@MotoScopeTheme
                }
                val state by runtime.telemetry.collectAsStateWithLifecycle()
                val recording by runtime.recording.state.collectAsStateWithLifecycle()
                val scope = rememberCoroutineScope()
                if (diagnosticsOpen) {
                    DiagnosticsRoute(onClose = { diagnosticsOpen = false })
                    return@MotoScopeTheme
                }
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
                        onOpenHistory = { historyOpen = true },
                        onOpenDiagnostics = { diagnosticsOpen = true }
                    )
                )
            }
        }
    }

    @Composable
    private fun DiagnosticsRoute(onClose: () -> Unit) {
        BackHandler(onBack = onClose)
        val state by diagnostics.state.collectAsStateWithLifecycle()
        DiagnosticsScreen(
            state = state,
            actions = DiagnosticsActions(onRun = diagnostics::run, onBack = onClose),
            simulated = diagnostics.simulated
        )
    }

    @Composable
    private fun HistoryRoute(
        openSessionId: String?,
        onOpenSession: (SessionId) -> Unit,
        onCloseSession: () -> Unit,
        onCloseHistory: () -> Unit
    ) {
        if (openSessionId == null) {
            BackHandler(onBack = onCloseHistory)
            val sessions by history.sessions.collectAsStateWithLifecycle()
            HistoryScreen(
                state = sessions,
                actions = HistoryActions(
                    onOpenSession = onOpenSession,
                    onDeleteSession = {},
                    onExportSession = { _, _ -> },
                    onBack = onCloseHistory
                )
            )
            return
        }
        BackHandler(onBack = onCloseSession)
        SessionDetailRoute(
            openSessionId = openSessionId,
            onOpenSession = onOpenSession,
            onCloseSession = onCloseSession
        )
    }

    @Composable
    private fun SessionDetailRoute(
        openSessionId: String,
        onOpenSession: (SessionId) -> Unit,
        onCloseSession: () -> Unit
    ) {
        val scope = rememberCoroutineScope()
        val id = SessionId(openSessionId)
        var detail by remember(openSessionId) {
            mutableStateOf<SessionDetailState>(SessionDetailState.Loading)
        }
        val notice by history.exportNotice.collectAsStateWithLifecycle()
        LaunchedEffect(openSessionId) {
            history.clearExportNotice()
            detail = history.detail(id)
        }
        val createCsv = rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument(ExportFormat.CSV.mediaType)
        ) { destination ->
            if (destination != null) history.export(id, ExportFormat.CSV, destination)
        }
        val createJson = rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument(ExportFormat.JSON.mediaType)
        ) { destination ->
            if (destination != null) history.export(id, ExportFormat.JSON, destination)
        }
        val startedAt = (detail as? SessionDetailState.Ready)
            ?.summary
            ?.session
            ?.startedAtEpochMillis
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
                onExportSession = { _, format ->
                    history.clearExportNotice()
                    val name = exportFileName(startedAt, format)
                    when (format) {
                        ExportFormat.CSV -> createCsv.launch(name)
                        ExportFormat.JSON -> createJson.launch(name)
                    }
                },
                onBack = onCloseSession
            ),
            notice = notice
        )
    }
}

private fun exportFileName(startedAtEpochMillis: Long?, format: ExportFormat): String {
    val stamp = startedAtEpochMillis
        ?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()) }
        ?.format(EXPORT_STAMP)
        ?: "sessao"
    return "motoscope-$stamp.${format.extension}"
}

private val EXPORT_STAMP: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmm", Locale.forLanguageTag("pt-BR"))
