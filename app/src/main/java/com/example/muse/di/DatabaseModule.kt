package com.example.muse.di

import android.content.Context
import androidx.room.Room
import com.example.muse.data.local.MyDatabase
import com.example.muse.data.local.dao.PlaylistDao
import com.example.muse.data.local.dao.PlaylistSongDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): MyDatabase {
        return Room.databaseBuilder(
            context,
            MyDatabase::class.java,
            "muse_database"
        ).build()
    }

    @Singleton
    @Provides
    fun providePlaylistDao(database: MyDatabase): PlaylistDao {
        return database.getPlaylistDao()
    }

    @Singleton
    @Provides
    fun providePlaylistSongDao(database: MyDatabase): PlaylistSongDao {
        return database.getPlaylistTrackDao()
    }

}