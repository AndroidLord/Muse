package com.example.muse.ui.muse

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import com.example.muse.data.local.model.Audio
import com.example.muse.data.local.model.Playlist
import com.example.muse.data.local.model.PlaylistSong


@Composable
fun PlaylistDetailScreen(
    startService: () -> Unit,
    viewModel: MusicViewModel
) {

    val context = LocalContext.current
    var audioList by remember { mutableStateOf(listOf<Audio>()) }

    val playlistSongs by viewModel.playlistSongs.collectAsState(initial = emptyList())

    LaunchedEffect(key1 = playlistSongs) {
        audioList = playlistSongs.map {
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
    }

    if (audioList.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No songs in this playlist")
        }
    } else {
        ReuseableMusicList(
            audioList = audioList,
            currentPlayingAudio = viewModel.currentSelectedAudio,
            onUIEvents = viewModel::onUIEvents,
            onMusicItemClick = {
                viewModel.onUIEvents(UIEvents.PlaylistSongPlay)
            },
            addSongToPlaylist = viewModel::addSongToPlaylist,
            addPlaylist = { viewModel.addPlaylist(Playlist(name = it)) },
            startService = startService,
            playlistFlow = viewModel.playlists
        )
    }
}