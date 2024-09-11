package com.example.muse.ui.muse.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.muse.ui.muse.MainScreen
import com.example.muse.ui.muse.MusicScreen
import com.example.muse.ui.muse.MusicViewModel

@Composable
fun MainNavGraph(
    navController: NavHostController,
    viewModel: MusicViewModel,
    startService: () -> Unit,
) {

    NavHost(
        navController = navController,
        startDestination = Routes.Main.route
    ) {

        composable(Routes.Main.route) {
            MainScreen(startService = startService, viewModel = viewModel, navController = navController)
        }

        composable(Routes.Music.route) {
            MusicScreen()
        }

//        composable(Routes.Home.route) {
//
//            HomeScreen(
//                startService = startService,
//                viewModel = viewModel,
//                navController = navController
//            )
//
//
//        }
//        composable(Routes.Playlist.route) {
//            PlaylistScreen(viewModel, navController)
//        }
//
//        composable(
//            route = Routes.PlaylistDetail.route,
//            arguments = listOf(
//                navArgument("playlistId") { type = NavType.LongType }
//            )
//        ) { navBackStackEntry ->
//
//            val playlistId = navBackStackEntry.arguments?.getLong("playlistId") ?: 0L
//            viewModel.updatePlaylistSongs(playlistId)
//
//            PlaylistDetailScreen(
//                startService = startService,
//                viewModel = viewModel
//            )
//
//        }
//
//        composable(Routes.Settings.route) {
//            // SettingsScreen()
//        }
    }


}