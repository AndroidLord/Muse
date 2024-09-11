package com.example.muse.ui.muse

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import com.example.muse.data.local.model.Playlist
import com.example.muse.ui.muse.navigation.Routes


@Composable
fun PlaylistScreen(
    viewModel: MusicViewModel,
    navController: NavController
) {

    val playlist: List<Playlist> by viewModel.playlists.collectAsState()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            Text(
                text = "Playlists",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            LazyColumn {
                items(playlist.size ) { index ->
                    ShowPlaylistItem(
                        modifier = Modifier.fillMaxWidth(),
                        playlist = playlist[index]
                    ){
                        navController.navigate(
                            Routes.PlaylistDetail.passPlaylistId(playlist[index].id)
                        )
                    }
                }
            }
        }
    }


}