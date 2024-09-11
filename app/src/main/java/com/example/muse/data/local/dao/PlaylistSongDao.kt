package com.example.muse.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.muse.data.local.model.PlaylistSong
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistSongDao {

// Playlist Songs -> CRUD operations

    // Create
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSongToPlaylist(track: PlaylistSong)

    // Read
    @Query("SELECT * FROM playlist_songs WHERE playlistId = :playlistId")
    fun getSongsFromPlaylist(playlistId: String): Flow<List<PlaylistSong>>

    // Delete
    @Delete
    suspend fun removeSongFromPlaylist(song: PlaylistSong)


}