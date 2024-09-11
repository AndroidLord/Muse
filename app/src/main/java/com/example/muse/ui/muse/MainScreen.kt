package com.example.muse.ui.muse

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.muse.data.local.model.Playlist
import com.example.muse.ui.muse.HelperUI.BottomBarPlayer
import com.example.muse.ui.muse.HelperUI.NoAudioFoundScreen
import com.example.muse.ui.muse.navigation.BottomNavGraph
import com.example.muse.ui.muse.navigation.Routes

@Composable
fun MainScreen(
    startService: () -> Unit,
    viewModel: MusicViewModel,
    navController: NavController
) {
    val isAudioPlaying = viewModel.isPlaying
    val audioList = viewModel.audioList
    val progress = viewModel.progress



    Log.d("MusicData", "Audio List Size: ${audioList.size}")

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp) // Adjust padding to avoid overlap with BottomAppBar
        ) {
            BottomNavGraph(
                navController = rememberNavController(),
                viewModel = viewModel,
                startService = startService
            )
        }

        BottomBarPlayer(
            isAudioPlaying = isAudioPlaying,
            progress = progress,
            onUiEvents = { viewModel.onUIEvents(it) },
            onBottomClick = {
                navController.navigate(Routes.Music.route)
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

    }
}