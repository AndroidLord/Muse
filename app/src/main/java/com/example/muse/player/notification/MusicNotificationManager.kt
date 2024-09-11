package com.example.muse.player.notification

import android.app.NotificationManager
import android.content.Context
import androidx.annotation.OptIn
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.ui.PlayerNotificationManager
import com.example.muse.R
import com.example.muse.utils.NOTIFICATION_CHANNEL_ID
import com.example.muse.utils.NOTIFICATION_CHANNEL_NAME
import com.example.muse.utils.NOTIFICATION_ID
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


class MusicNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val exoPlayer: ExoPlayer
) {

    private val notificationManager: NotificationManagerCompat =
        NotificationManagerCompat.from(context)

    init {
        createNotificationChannel()
    }


    fun startNotificationService(
        mediaSessionService: MediaSessionService,
        mediaSession: MediaSession
    ) {
        buildNotification(mediaSession)
        startForegroundNotificationService(mediaSessionService)
    }

    private fun startForegroundNotificationService(
        mediaSessionService: MediaSessionService
    ) {
        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
        mediaSessionService.startForeground(NOTIFICATION_ID, notification)
    }

    @OptIn(UnstableApi::class)
    private fun buildNotification(
        mediaSession: MediaSession
    ) {

        val notificationBuilder = PlayerNotificationManager.Builder(
            context,
            NOTIFICATION_ID,
            NOTIFICATION_CHANNEL_ID
        ).apply {
            setMediaDescriptionAdapter(NotificationMediaDescriptionAdapter(context = context,pendingIntent = mediaSession.sessionActivity))
            setSmallIconResourceId(R.drawable.ic_launcher_background)
        }.build().also {
            it.setMediaSessionToken(mediaSession.platformToken) // Moved from second apply block
            it.setUseFastForwardActionInCompactView(true)
            it.setUseRewindActionInCompactView(true)
            it.setUseNextActionInCompactView(true)
            it.setPriority(NotificationCompat.PRIORITY_LOW)
            it.setPlayer(exoPlayer)
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannelCompat.Builder(
            NOTIFICATION_CHANNEL_ID,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            setName(NOTIFICATION_CHANNEL_NAME)
        }.build()
        notificationManager.createNotificationChannel(channel)
    }

}