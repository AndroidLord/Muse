package com.example.muse.data.repository

import android.util.Log
import com.example.muse.data.local.ContentResolverHelper
import com.example.muse.data.local.model.Audio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MusicRepository @Inject constructor(
    private val contentResolver: ContentResolverHelper
) {

    suspend fun getAudioList(): List<Audio> = withContext(Dispatchers.IO) {
        Log.d("MusicData", "getAudioList: Fetching Audio List")
        val audioList = contentResolver.getAudioData()
        Log.d("MusicData", "size: ${audioList.size}")
        audioList
    }

}

