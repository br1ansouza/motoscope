package dev.br1ansouza.motoscope.feature.recording

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import javax.inject.Inject

internal class RecordingNotifications @Inject constructor(private val context: Context) {
    fun ensureChannel() {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.recording_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.recording_channel_description)
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }

    fun build(launchIntent: Intent?): Notification {
        val builder = Notification.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.recording_notification_title))
            .setContentText(context.getString(R.string.recording_notification_text))
            .setSmallIcon(android.R.drawable.presence_video_online)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(Notification.CATEGORY_SERVICE)
            .addAction(stopAction())
        if (launchIntent != null) {
            builder.setContentIntent(activityIntent(launchIntent))
        }
        return builder.build()
    }

    private fun stopAction(): Notification.Action = Notification.Action.Builder(
        null,
        context.getString(R.string.recording_notification_stop),
        stopIntent()
    ).build()

    private fun activityIntent(intent: Intent): PendingIntent = PendingIntent.getActivity(
        context,
        REQUEST_OPEN,
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    private fun stopIntent(): PendingIntent = PendingIntent.getService(
        context,
        REQUEST_STOP,
        Intent(context, RecordingService::class.java).setAction(RecordingService.ACTION_STOP),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    companion object {
        const val CHANNEL_ID = "motoscope.recording"
        const val NOTIFICATION_ID = 1001
        private const val REQUEST_OPEN = 1
        private const val REQUEST_STOP = 2
    }
}
