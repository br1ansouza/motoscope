package dev.br1ansouza.motoscope.feature.recording

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint
import dev.br1ansouza.motoscope.core.recording.RecordingEngine
import dev.br1ansouza.motoscope.core.recording.RecordingState
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RecordingService : Service() {
    @Inject
    internal lateinit var engine: RecordingEngine

    @Inject
    internal lateinit var notifications: RecordingNotifications

    private val scope = CoroutineScope(SupervisorJob())
    private var stateJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        notifications.ensureChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()
            else -> stop()
        }
        return START_STICKY
    }

    private fun start() {
        promote(engine.state.value)
        if (stateJob != null) return
        stateJob = scope.launch {
            engine.start()
            engine.state.collectLatest { state ->
                if (state is RecordingState.Active) promote(state)
            }
        }
    }

    private fun stop() {
        scope.launch {
            engine.stop()
            stateJob?.cancel()
            stateJob = null
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun promote(state: RecordingState) {
        val launch = packageManager.getLaunchIntentForPackage(packageName)
        val notification = notifications.build(state, launch)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                RecordingNotifications.NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(RecordingNotifications.NOTIFICATION_ID, notification)
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "dev.br1ansouza.motoscope.recording.START"
        const val ACTION_STOP = "dev.br1ansouza.motoscope.recording.STOP"

        fun start(context: android.content.Context) {
            val intent = Intent(context, RecordingService::class.java).setAction(ACTION_START)
            context.startForegroundService(intent)
        }

        fun stop(context: android.content.Context) {
            val intent = Intent(context, RecordingService::class.java).setAction(ACTION_STOP)
            context.startService(intent)
        }
    }
}
