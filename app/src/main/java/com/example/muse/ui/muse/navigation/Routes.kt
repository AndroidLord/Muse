package com.example.muse.ui.muse.navigation

sealed class Routes(val route: String){

    object Main: Routes("main")
    object Music: Routes("music")

    object Home: Routes("home")

    object Playlist: Routes("playlist")

    object PlaylistDetail: Routes("playlistDetail/{playlistId}"){
        fun passPlaylistId(playlistId: Long): String{
            return "playlistDetail/$playlistId"
        }
    }

    object Settings: Routes("settings")


}
