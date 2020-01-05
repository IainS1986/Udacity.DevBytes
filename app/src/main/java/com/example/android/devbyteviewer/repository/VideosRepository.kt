/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.example.android.devbyteviewer.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.android.devbyteviewer.database.VideoDatabase
import com.example.android.devbyteviewer.database.asDomainModel
import com.example.android.devbyteviewer.domain.Video
import com.example.android.devbyteviewer.network.Network
import com.example.android.devbyteviewer.network.asDatabaseModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class VideosRepository(private val database: VideoDatabase) {

    /**
     * A playlist of videos that can be shown in app
     *
     * This returns the videos in the database, converting it to the App/Domain model required
     */
    val videos: LiveData<List<Video>> = Transformations.map(database.videoDao.getVideos()) {
        it.asDomainModel()
    }

    /**
     * Refresh the videos in the offline cache
     *
     * Will run on IO thread and will trigger a network call to fetch new videos
     */
    suspend fun refreshVideos() {

        // Run on the IO dispatcher thread as we are reading/writing to disk (Database insert)
        withContext(Dispatchers.IO){

            // Request
            val playlist = Network.devbytes.getPlaylist().await()

            // Store Note - * is the "spread" operator and allows arrays to act as varargs (params). Its NOT pointer magic.
            database.videoDao.insertAll(*playlist.asDatabaseModel())

        }
    }
}