package com.example.muse.player.service


import android.content.Intent
import androidx.media3.common.Player
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.muse.player.notification.MusicNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MusicService : MediaSessionService() {

    @Inject lateinit var mediaSession: MediaSession

    @Inject lateinit var notificationManager: MusicNotificationManager

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        notificationManager.startNotificationService(
            mediaSession = mediaSession,
            mediaSessionService = this
        )

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession = mediaSession

    override fun onDestroy() {
        super.onDestroy()

        mediaSession.release()
        when(mediaSession.player.playbackState) {
            Player.STATE_IDLE -> Unit
            else -> {
                mediaSession.player.seekTo(0)
                mediaSession.player.playWhenReady = false
                mediaSession.player.stop()
            }
        }

    }
}