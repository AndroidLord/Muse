package com.example.muse.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.muse.data.local.model.Playlist
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

// Playlist -> CRUD operations

    // Create
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createPlaylist(playlist: Playlist)

    // Read
    @Query("SELECT * FROM playlists")
    fun getAllPlaylists(): Flow<List<Playlist>>

    // Update
    @Update
    suspend fun updatePlaylist(playlist: Playlist)

    // Delete
    @Delete
    suspend fun deletePlaylist(playlist: Playlist)


}