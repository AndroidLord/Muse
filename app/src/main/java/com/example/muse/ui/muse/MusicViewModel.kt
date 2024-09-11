package com.example.muse.ui.muse

import android.util.Log
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.example.muse.data.local.dao.PlaylistDao
import com.example.muse.data.local.dao.PlaylistSongDao
import com.example.muse.data.local.model.Audio
import com.example.muse.data.local.model.Playlist
import com.example.muse.data.local.model.PlaylistSong
import com.example.muse.data.repository.MusicRepository
import com.example.muse.player.service.MusicServiceHandler
import com.example.muse.player.service.MusicState
import com.example.muse.player.service.PlayerEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

val audioDummy = Audio(
    uri = "".toUri(), displayName = "", id = 0L, artist = "", data = "", duration = 0, title = ""
)


@OptIn(SavedStateHandleSaveableApi::class)
@HiltViewModel
class MusicViewModel @Inject constructor(
    private val musicServiceHandler: MusicServiceHandler,
    private val musicRepository: MusicRepository,
    savedStateHandle: SavedStateHandle,
    private val playlistDao: PlaylistDao,
    private val playlistSongDao: PlaylistSongDao
) : ViewModel() {

    var duration by savedStateHandle.saveable { mutableLongStateOf(0L) }
    var progress by savedStateHandle.saveable { mutableFloatStateOf(0f) }
    var progressString by savedStateHandle.saveable { mutableStateOf("00:00") }
    var isPlaying by savedStateHandle.saveable { mutableStateOf(false) }
    var currentSelectedAudio by savedStateHandle.saveable { mutableStateOf(audioDummy) }
    var audioList by savedStateHandle.saveable { mutableStateOf(listOf<Audio>()) }

    private val _uiState: MutableStateFlow<UIState> = MutableStateFlow(UIState.Initial)
    val uiState: StateFlow<UIState> = _uiState.asStateFlow()

    init {
        LoadAudioData()
    }

    // This is a coroutine that listens to the music state and updates the UI accordingly
    init {

        viewModelScope.launch {
            musicServiceHandler.musicState.collectLatest { musicState ->
                when (musicState) {

                    MusicState.Initial -> {
                        _uiState.value = UIState.Initial
                    }

                    is MusicState.Buffering -> {
                        calculateProgressValue(musicState.progress)
                    }

                    is MusicState.Playing -> {
                        isPlaying = musicState.isPlaying
                    }

                    is MusicState.Progress -> {
                        calculateProgressValue(musicState.progress)
                    }

                    is MusicState.CurrentPlayingAudio -> {
                        currentSelectedAudio = audioList[musicState.mediaItemIndex]
                    }

                    is MusicState.Ready -> {
                        duration = musicState.duration
                        _uiState.value = UIState.Ready
                    }
                }
            }
        }
    }

    fun onUIEvents(uiEvent: UIEvents) = viewModelScope.launch {
        when (uiEvent) {
            UIEvents.Backward -> musicServiceHandler.onPlayerEvents(PlayerEvent.Backward)
            UIEvents.Forward -> musicServiceHandler.onPlayerEvents(PlayerEvent.Forward)
            UIEvents.SeekToNext -> musicServiceHandler.onPlayerEvents(PlayerEvent.SeekToNext)
            UIEvents.PlayPause -> musicServiceHandler.onPlayerEvents(PlayerEvent.PlayPause)
            is UIEvents.SeekTo -> {
                musicServiceHandler.onPlayerEvents(
                    playerEvent = PlayerEvent.SeekTo,
                    seekPosition = ((duration * uiEvent.position) / 100f).toLong()
                )
            }

            is UIEvents.SelectAudioChange -> {
                musicServiceHandler.onPlayerEvents(
                    playerEvent = PlayerEvent.SelectedAudioChange,
                    selectedAudioIndex = uiEvent.index
                )
            }

            is UIEvents.UpdateProgress -> {
                musicServiceHandler.onPlayerEvents(
                    playerEvent = PlayerEvent.UpdateProgress(uiEvent.newProgress)
                )
                progress = uiEvent.newProgress
            }

            UIEvents.Repeat -> TODO()
            UIEvents.SeekToPrevious -> TODO()
            UIEvents.Shuffle -> TODO()
            is UIEvents.onForeground -> {
                musicServiceHandler.onPlayerEvents(PlayerEvent.Foreground(uiEvent.isForeground))
            }

            UIEvents.PlaylistSongPlay -> {
                viewModelScope.launch {
                    updateMediaItemsFromPlaylist()
                }
            }
        }
    }

    private fun LoadAudioData() {
        viewModelScope.launch {
            audioList = musicRepository.getAudioList()
            Log.d("MusicViewModel", "Got the Data in ViewModel, LoadAudioData: $audioList")
            setMediaItems()
        }
    }

    private fun setMediaItems() {
        audioList.map { audio ->
            MediaItem.Builder()
                .setUri(audio.uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setAlbumTitle(audio.title)
                        .setDisplayTitle(audio.displayName)
                        .setSubtitle(audio.displayName)
                        .build()
                ).build()
        }.also {
            musicServiceHandler.clearMediaItems()
            Log.d("MusicViewModel", "Passed Data to MediaItems, setMediaItems: $it")
            musicServiceHandler.setMediaItemList(it)
        }
    }

    private suspend fun updateMediaItemsFromPlaylist() = withContext(Dispatchers.Main) {
        if (playlistSongs.value.isEmpty()) return@withContext
        Log.d("Muse_Playlist", "Inside updateMediaItemsFromPlaylist")
        audioList = playlistSongs.value.map {
            Audio(
                uri = it.songUri.toUri(),
                id = it.id,
                title = it.songTitle,
                artist = it.songArtist,
                displayName = it.songName,
                data = it.songData,
                duration = it.songDuration,
            )
        }
        setMediaItems()
    }

    private fun calculateProgressValue(currentProgress: Long) {

        progress = when {
            currentProgress > 0 -> (currentProgress.toFloat() / duration.toFloat()) * 100
            else -> 0f
        }
        progressString = formatDuration(currentProgress)
    }

    fun formatDuration(duration: Long): String {
        val minute = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
        val seconds = (minute) - minute * TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES)
        return String.format("%02d:%02d", minute, seconds)
    }

    override fun onCleared() {
        viewModelScope.launch {
            musicServiceHandler.onPlayerEvents(PlayerEvent.Stop)
        }
        super.onCleared()
    }


    // ---------------------------->Playlist things from here<----------------------------


    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> get() = _playlists

    private val _playlistSongs = MutableStateFlow<List<PlaylistSong>>(emptyList())
    val playlistSongs: StateFlow<List<PlaylistSong>> get() = _playlistSongs

    init {
        viewModelScope.launch {
            fetchAllPlaylists()
            playlists.value.forEach {
                Log.d("Muse_Playlist", "The Playlist are ${it.name}")
            }
        }
    }

    // CRUD operations for Playlist
    fun addPlaylist(playlist: Playlist) {
        viewModelScope.launch(Dispatchers.IO) {
            playlistDao.createPlaylist(playlist)
            fetchAllPlaylists()
        }
    }

    private fun fetchAllPlaylists() {
        viewModelScope.launch(Dispatchers.IO) {
            playlistDao.getAllPlaylists().collect { playlists ->
                _playlists.value = playlists
            }
        }
    }

    fun updatePlaylist(playlist: Playlist) {
        viewModelScope.launch(Dispatchers.IO) {
            playlistDao.updatePlaylist(playlist)
            fetchAllPlaylists()
        }
    }

    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch(Dispatchers.IO) {
            playlistDao.deletePlaylist(playlist)
            fetchAllPlaylists()
        }
    }

    // CRUD operations for PlaylistTrack

    // Create
    fun addSongToPlaylist(playlistSong: PlaylistSong) {
        viewModelScope.launch(Dispatchers.IO) {
            playlistSongDao.addSongToPlaylist(playlistSong)
        }
    }

    fun updatePlaylistSongs(playlistId: Long) {
        Log.d("Muse_Playlist", "The _Playlist size ${_playlistSongs.value.size}")
        Log.d("Muse_Playlist", "The Playlist size ${playlistSongs.value.size}")
        fetchSongsFromPlaylist(playlistId)
        //onUIEvents(UIEvents.PlaylistSongPlay)
        Log.d("Muse_Playlist", "Updated _Playlist size ${_playlistSongs.value.size}")
        Log.d("Muse_Playlist", "Updated Playlist size ${playlistSongs.value.size}")
    }

    private fun fetchSongsFromPlaylist(playlistId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            playlistSongDao.getSongsFromPlaylist(playlistId.toString()).collect { playlistSong: List<PlaylistSong> ->
                _playlistSongs.value = playlistSong
//                updateMediaItemsFromPlaylist()
            }
        }
    }

    fun removeTrackFromPlaylist(playlistSong: PlaylistSong) {
        viewModelScope.launch(Dispatchers.IO) {
            playlistSongDao.removeSongFromPlaylist(playlistSong)
        }
    }

}

sealed class UIEvents {
    object PlayPause : UIEvents()
    data class SelectAudioChange(val index: Int) : UIEvents()
    data class SeekTo(val position: Float) : UIEvents()
    object SeekToNext : UIEvents()
    object SeekToPrevious : UIEvents()
    object Forward : UIEvents()
    object Backward : UIEvents()
    data class UpdateProgress(val newProgress: Float) : UIEvents()
    object Shuffle : UIEvents()
    object Repeat : UIEvents()
    data class onForeground(val isForeground: Boolean) : UIEvents()
    object PlaylistSongPlay : UIEvents()
}

sealed class UIState {
    object Initial : UIState()
    object Ready : UIState()
}