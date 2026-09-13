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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RecordingService : Service() {
    @Inject
    internal lateinit var engine: RecordingEngine

    @Inject
    internal lateinit var notifications: RecordingNotifications

    @Inject
    internal lateinit var applicationScope: CoroutineScope

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val commands = Channel<Command>(Channel.UNLIMITED)
    private var latestStartId = 0

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        notifications.ensureChannel()
        scope.launch { for (command in commands) execute(command) }
        scope.launch {
            engine.state.collect { state ->
                if (state == RecordingState.Failed) notifications.showFailure()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        latestStartId = startId
        val action = when (intent?.action) {
            ACTION_START -> Action.START
            ACTION_STOP -> Action.STOP
            null -> if (intent == null) Action.RESTORE else null
            else -> null
        }
        if (action == null) {
            if (engine.state.value == RecordingState.Idle) stopSelfResult(startId)
            return START_NOT_STICKY
        }
        if (action == Action.START || action == Action.RESTORE) promote()
        commands.trySend(Command(action, startId))
        return if (action == Action.STOP) START_NOT_STICKY else START_STICKY
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun execute(command: Command) {
        try {
            when (command.action) {
                Action.START -> engine.start()
                Action.RESTORE -> if (!engine.restore()) finishService(command.startId)
                Action.STOP -> {
                    engine.stop()
                    finishService(command.startId)
                }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            engine.reportFailure()
            notifications.showFailure()
        }
    }

    override fun onTimeout(startId: Int, fgsType: Int) {
        finishService(latestStartId)
    }

    private fun finishService(startId: Int) {
        if (startId == latestStartId) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelfResult(startId)
        }
    }

    private fun promote() {
        val launch = packageManager.getLaunchIntentForPackage(packageName)
        val notification = notifications.build(launch)
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

    @Suppress("TooGenericExceptionCaught")
    override fun onDestroy() {
        commands.close()
        scope.cancel()
        applicationScope.launch(start = CoroutineStart.UNDISPATCHED) {
            try {
                engine.interrupt()
            } catch (_: Exception) {
                engine.reportFailure()
            }
        }
        super.onDestroy()
    }

    private data class Command(val action: Action, val startId: Int)

    private enum class Action { START, RESTORE, STOP }

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
