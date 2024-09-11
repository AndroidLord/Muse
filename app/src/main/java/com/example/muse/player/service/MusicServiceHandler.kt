package com.example.muse.player.service

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class MusicServiceHandler @Inject constructor(
    private val exoplayer: ExoPlayer
) : Player.Listener {

    private val _musicState: MutableStateFlow<MusicState> = MutableStateFlow(MusicState.Initial)
    val musicState: StateFlow<MusicState> = _musicState.asStateFlow()

    init {
        exoplayer.addListener(this)
    }

    private var job: Job? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun addMediaItem(mediaItem: MediaItem) {
        exoplayer.setMediaItem(mediaItem)
        exoplayer.prepare()
    }

    fun setMediaItemList(mediaItems: List<MediaItem>) {
        exoplayer.setMediaItems(mediaItems)
        exoplayer.prepare()
    }

    fun clearMediaItems() {
        exoplayer.clearMediaItems()
    }

    suspend fun onPlayerEvents(
        playerEvent: PlayerEvent,
        selectedAudioIndex: Int = -1,
        seekPosition: Long = 0L
    ) {

        when (playerEvent) {
            PlayerEvent.Backward -> {
                exoplayer.seekBack()
            }

            PlayerEvent.Forward -> {
                exoplayer.seekForward()
            }

            PlayerEvent.SeekToNext -> {
                exoplayer.seekToNext()
            }

            PlayerEvent.PlayPause -> {
                PlayOrPause()
            }

            PlayerEvent.SeekTo -> {
                exoplayer.seekTo(seekPosition)
            }

            PlayerEvent.SelectedAudioChange -> {
                onSelectedAudioChange(selectedAudioIndex)
            }

            PlayerEvent.Stop -> {
                stopProgressUpdate()
            }

            is PlayerEvent.UpdateProgress -> {
                exoplayer.seekTo(
                    (exoplayer.duration * playerEvent.newProgress).toLong()
                )
            }

            is PlayerEvent.Foreground -> {
                when {
                    playerEvent.isForeground && exoplayer.isPlaying -> {
                        startProgressUpdate()
                    }
                    !playerEvent.isForeground && exoplayer.isPlaying -> {
                        stopProgressUpdate(playerEvent.isForeground)
                    }
                }
            }
        }

    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            ExoPlayer.STATE_BUFFERING -> {
                _musicState.value = MusicState.Buffering(exoplayer.bufferedPosition)
            }

            ExoPlayer.STATE_READY -> {
                _musicState.value = MusicState.Ready(exoplayer.duration)
            }
            else -> Unit
        }
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        super.onMediaItemTransition(mediaItem, reason)
        _musicState.value = MusicState.CurrentPlayingAudio(exoplayer.currentMediaItemIndex)
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        _musicState.value = MusicState.Playing(isPlaying = isPlaying)
        _musicState.value = MusicState.CurrentPlayingAudio(exoplayer.currentMediaItemIndex)

        when (isPlaying) {
            true -> {
                scope.launch { startProgressUpdate() }
            }

            false -> {
                stopProgressUpdate()
            }
        }
    }

    private suspend fun onSelectedAudioChange(selectedAudioIndex: Int) {

        when (selectedAudioIndex) {
            exoplayer.currentMediaItemIndex -> {
                PlayOrPause()
            }

            else -> {
                exoplayer.seekToDefaultPosition(selectedAudioIndex)
                _musicState.value = MusicState.Playing(isPlaying = true)
                exoplayer.playWhenReady = true
                startProgressUpdate()
            }
        }
    }

    private suspend fun PlayOrPause() {
        if (exoplayer.isPlaying) {
            exoplayer.pause()
            stopProgressUpdate()
        } else {
            exoplayer.play()
            _musicState.value = MusicState.Playing(isPlaying = true)
            startProgressUpdate()
        }
    }

//    private suspend fun startProgressUpdate() = job.run {
//        while (true){
//            delay(500)
//            _musicState.value = MusicState.Progress(exoplayer.currentPosition)
//        }
//    }

    private suspend fun startProgressUpdate() = scope.launch {
        while (true) {
            delay(500)
            _musicState.value = MusicState.Progress(exoplayer.currentPosition)
        }
    }

    //    private fun stopProgressUpdate(){
//        job?.cancel()
//        _musicState.value = MusicState.Playing(isPlaying = false)
//    }
//
    private fun stopProgressUpdate(
        isForeground: Boolean = true
    ) {
        scope.coroutineContext.cancelChildren()
        if (isForeground)
            _musicState.value = MusicState.Playing(isPlaying = false) // Update state if closing
    }
}

sealed class PlayerEvent {
    object PlayPause : PlayerEvent()
    object SelectedAudioChange : PlayerEvent()
    object Backward : PlayerEvent()
    object Forward : PlayerEvent()
    object SeekToNext : PlayerEvent()
    object SeekTo : PlayerEvent()
    object Stop : PlayerEvent()
    data class UpdateProgress(val newProgress: Float) : PlayerEvent()
    data class Foreground(val isForeground: Boolean) : PlayerEvent()
}

sealed class MusicState {
    object Initial : MusicState()
    data class Ready(val duration: Long) : MusicState()
    data class Progress(val progress: Long) : MusicState()
    data class Buffering(val progress: Long) : MusicState()
    data class Playing(val isPlaying: Boolean) : MusicState()
    data class CurrentPlayingAudio(val mediaItemIndex: Int) : MusicState()
}