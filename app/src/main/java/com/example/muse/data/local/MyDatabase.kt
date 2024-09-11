package com.example.muse.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.muse.data.local.dao.PlaylistDao
import com.example.muse.data.local.dao.PlaylistSongDao
import com.example.muse.data.local.model.Playlist
import com.example.muse.data.local.model.PlaylistSong

@Database(
    entities = [Playlist::class, PlaylistSong::class],
    version = 1,
    exportSchema = false
)
abstract class MyDatabase: RoomDatabase() {

    abstract fun getPlaylistDao(): PlaylistDao
    abstract fun getPlaylistTrackDao(): PlaylistSongDao

}