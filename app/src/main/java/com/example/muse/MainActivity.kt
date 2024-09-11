package com.example.muse

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.muse.player.service.MusicService
import com.example.muse.ui.muse.HomeScreen
import com.example.muse.ui.muse.MusicViewModel
import com.example.muse.ui.muse.UIEvents
import com.example.muse.ui.muse.navigation.MainNavGraph
import com.example.muse.ui.theme.MuseTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {


    private var isServiceRunning = false

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MuseTheme {

                val viewModel = viewModel(modelClass = MusicViewModel::class.java)

//                val uiStateFlow = viewModel.uiState.collectAsState().value
//                val audioList = viewModel.audioList
//                val playlistFlow = viewModel.playlists

                val permissions = listOf(
                    Manifest.permission.FOREGROUND_SERVICE,
                    when {
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> Manifest.permission.READ_MEDIA_AUDIO
                        else -> Manifest.permission.READ_EXTERNAL_STORAGE
                    },

                    ).filterNotNull()


                val permissionStates = rememberMultiplePermissionsState(permissions = permissions)

                val lifecycleOwner = LocalLifecycleOwner.current

                DisposableEffect(key1 = lifecycleOwner) {

                    val observer = LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_START -> {
                                viewModel.onUIEvents(UIEvents.onForeground(true))
                            }

                            Lifecycle.Event.ON_RESUME -> {
                                permissionStates.launchMultiplePermissionRequest()
                            }

                            Lifecycle.Event.ON_STOP -> {
                                viewModel.onUIEvents(UIEvents.onForeground(false))
                            }

                            else -> Unit
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    when (permissionStates.allPermissionsGranted) {
                        true -> {
                            MainNavGraph(
                                navController = rememberNavController(),
                                viewModel = viewModel,
                                startService = { startService() }
                            )
                        }
                        else -> {
                            PermissionScreen()
                        }
                    }
                }
            }
        }
    }

    private fun startService() {
        if (!isServiceRunning) {
            Log.d("MusicData", "Starting Service")
            val intent = Intent(this, MusicService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            isServiceRunning = true
        }
    }
}

@Composable
fun PermissionScreen(modifier: Modifier = Modifier) {

    Box(
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Grant Permission Required to Use the App"
        )
    }

}




