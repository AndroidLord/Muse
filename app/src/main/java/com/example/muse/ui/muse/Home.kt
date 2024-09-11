package com.example.muse.ui.muse

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.muse.data.local.model.Audio
import com.example.muse.data.local.model.Playlist
import com.example.muse.data.local.model.PlaylistSong
import com.example.muse.ui.muse.HelperUI.CreatePlaylistDialog
import com.example.muse.ui.muse.HelperUI.IconWithText
import com.example.muse.ui.muse.HelperUI.MusicColumnList
import com.example.muse.ui.muse.HelperUI.NoAudioFoundScreen
import com.example.muse.ui.muse.navigation.Routes
import com.example.muse.utils.MoreInfoEvents
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    startService: () -> Unit,
    viewModel: MusicViewModel,
    navController: NavController
) {

    val audioList = viewModel.audioList
    val playlistFlow = viewModel.playlists
    val currentPlayingAudio = viewModel.currentSelectedAudio


    Scaffold{ innerPadding ->

        Column(
            modifier = modifier.padding(innerPadding),
        ){

            Column {
                Text(
                    text = "Top Playlist",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(8.dp)
                )
                LazyRow() {
                    item() {
                        ShowPlaylistItem(playlist = Playlist(name = "Recent"))
                        ShowPlaylistItem(playlist = Playlist(name = "Favorite"))
                        ShowPlaylistItem(playlist = Playlist(name = "More +")) {
                            navController.navigate(Routes.Playlist.route)
                        }
                    }
                }
            }

            if (audioList.isNotEmpty()) {
                ReuseableMusicList(
                    audioList = audioList,
                    currentPlayingAudio = currentPlayingAudio,
                    onUIEvents = viewModel::onUIEvents,
                    addSongToPlaylist = viewModel::addSongToPlaylist,
                    addPlaylist = { viewModel.addPlaylist(Playlist(name = it)) },
                    startService = startService,
                    playlistFlow = playlistFlow
                )

            } else {
                NoAudioFoundScreen()
            }

        }



    }


}

@Composable
fun ReuseableMusicList(
    audioList: List<Audio>,
    currentPlayingAudio: Audio,
    onUIEvents: (UIEvents) -> Unit,
    onMusicItemClick: () -> Unit = {},
    addSongToPlaylist: (song: PlaylistSong) -> Unit,
    addPlaylist: (String) -> Unit,
    startService: () -> Unit,
    playlistFlow: StateFlow<List<Playlist>>,
) {

    var moreInfoState by remember { mutableStateOf(false) }
    var trackIdState by remember { mutableStateOf(-1L) }

    var createPlaylistDialogState by remember { mutableStateOf(false) }

    var currentPlayingAudioState by remember { mutableStateOf(currentPlayingAudio) }


    LaunchedEffect(key1 = currentPlayingAudio) {
//        Toast.makeText(context, "Current Audio: ${currentPlayingAudio.displayName}", Toast.LENGTH_SHORT).show()
        Log.d("MusicCompose", "**************Current Audio: ${currentPlayingAudio.displayName}")
        currentPlayingAudioState = currentPlayingAudio
    }



    if (createPlaylistDialogState) {
        CreatePlaylistDialog(
            onDismiss = { createPlaylistDialogState = false },
            onPlaylistCreateClick = { playlistName: String, songId: Long ->
                createPlaylistDialogState = false
                addPlaylist(playlistName)
                //addPlaylist.invoke(playlistName)
            }
        )
    }

    MusicColumnList(
        audioList = audioList,
        currentPlayingAudio = currentPlayingAudio,
        onMoreInfoClick = { id: Long ->
            moreInfoState = true
            trackIdState = id
        },
        onMusicItemClick = onMusicItemClick,
        onUiEvents = onUIEvents ,
        startService = startService
    )

    // More Info Bottom Sheet
    if (moreInfoState) {
        moreInfoAboutSong(
            trackId = trackIdState,
            playlists = playlistFlow,
            onDismiss = {
                trackIdState = -1
                moreInfoState = false
            },
            onPlaylistCreate = {
                createPlaylistDialogState = true
            },
            addSongToPlaylist = { playlistId, songId ->
                val audio = audioList.find { it.id == songId }
                val song = PlaylistSong(
                    playlistId = playlistId,
                    songId = songId,
                    songName = audio?.displayName ?: "Unknown",
                    songArtist = audio?.artist ?: "Unknown",
                    songUri = audio?.uri.toString(),
                    songDuration = audio?.duration ?: 0
                )
                addSongToPlaylist(song)
                trackIdState = -1
                moreInfoState = false
            }
        )
    }
}

@Composable
fun moreInfoAboutSong(
    trackId: Long,
    playlists: Flow<List<Playlist>>,
    onPlaylistCreate: (Long) -> Unit,
    onDismiss: () -> Unit,
    addSongToPlaylist: (Long, Long) -> Unit,
    moreInfoEvents: MoreInfoEvents = MoreInfoEvents.Details
) {
    var moreInfoEventState by remember {
        mutableStateOf<MoreInfoEvents>(moreInfoEvents)
    }

    when (moreInfoEventState) {
        MoreInfoEvents.Details -> {
            MoreInfoDetailBottomSheet(
                onDismiss,
                trackId,
                playlists,
                onPlaylistCreate,
                onPlaylistClick = {
                    moreInfoEventState = MoreInfoEvents.ADD_TO_PLAYLIST
                })
        }

        MoreInfoEvents.PlayNext -> TODO()
        MoreInfoEvents.ADD_TO_PLAYLIST -> {
            AddToPlaylistModalBottomSheetLayout(
                playlistFlow = playlists,
                onCreateNewPlaylistClick = onPlaylistCreate,
                songId = trackId,
                onDismiss = onDismiss,
                addSongToPlaylist = addSongToPlaylist
            )
        }

        MoreInfoEvents.INFO -> TODO()
        MoreInfoEvents.SHARE -> TODO()
        MoreInfoEvents.DELETE -> TODO()
        else -> Unit
    }

}


@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun MoreInfoDetailBottomSheet(
    onDismiss: () -> Unit,
    trackId: Long,
    playlists: Flow<List<Playlist>>,
    onPlaylistCreate: (Long) -> Unit,
    onPlaylistClick: (Long) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = { onDismiss() }
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {

            Text(
                text = "Cancel",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Red,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { onDismiss() }
                    .fillMaxWidth()
                    .align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(16.dp))
            IconWithText(icon = Icons.Default.SkipNext, text = "Play next")

            Spacer(modifier = Modifier.height(16.dp))
            IconWithText(icon = Icons.AutoMirrored.Default.PlaylistAdd, text = "Add To Playlist") {
                onPlaylistClick(trackId)
            }

            Spacer(modifier = Modifier.height(16.dp))
            IconWithText(icon = Icons.Outlined.Info, text = "Song Info")


            Spacer(modifier = Modifier.height(16.dp))
            IconWithText(icon = Icons.Outlined.Share, text = "Share")


            Spacer(modifier = Modifier.height(16.dp))
            IconWithText(icon = Icons.Outlined.Delete, text = "Delete")

        }

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddToPlaylistModalBottomSheetLayout(
    playlistFlow: Flow<List<Playlist>>,
    onCreateNewPlaylistClick: (Long) -> Unit,
    addSongToPlaylist: (Long, Long) -> Unit,
    songId: Long,
    onDismiss: () -> Unit
) {
    val playlistState by playlistFlow.collectAsState(initial = emptyList())

    ModalBottomSheet(onDismissRequest = { onDismiss() }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {


            Text(
                text = "Cancel",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Red,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { onDismiss() }
                    .fillMaxWidth()
                    .align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(16.dp))

            IconWithText(
                icon = Icons.Outlined.Add,
                text = "Create New Playlist"
            ) {
                onCreateNewPlaylistClick(songId)
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn() {

                itemsIndexed(playlistState) { index: Int, playlist: Playlist ->

                    IconWithText(
                        icon = Icons.AutoMirrored.Outlined.QueueMusic,
                        text = playlist.name,
                    ) {
                        addSongToPlaylist(playlist.id, songId)
                    }

                }

            }


        }
    }

}


@Composable
fun ShowPlaylistItem(
    modifier: Modifier = Modifier
        .width(120.dp)
        .height(70.dp),
    playlist: Playlist,
    color : Color = Color.Black,
    onPlaylistClick: (Playlist) -> Unit = {}
) {
    Card(
        modifier = modifier
            .padding(8.dp)
            .clickable { onPlaylistClick(playlist) },
        colors = CardDefaults.cardColors(
            contentColor = Color.White,
            containerColor = color
        )
    ) {
        Box(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.titleMedium,
                overflow = TextOverflow.Clip,
                maxLines = 1,
                modifier = Modifier
            )
        }
    }
}