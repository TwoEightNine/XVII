/*
 * xvii - messenger for vk
 * Copyright (C) 2021  TwoEightNine
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.twoeightnine.root.xvii.background.music.utils

import android.content.Context
import com.twoeightnine.root.xvii.background.music.models.Track
import com.twoeightnine.root.xvii.model.attachments.Audio
import java.io.File

/**
 * helps store [Audio] in cache using [Track] as an actual model
 */
class TrackManager(private val context: Context) {

    private val dir = File(context.cacheDir, TRACK_DIR)

    init {
        if (!dir.exists()) {
            dir.mkdir()
        }
    }

    fun getFile(audio: Audio) = File(dir, audio.fullId + ".mp3")

    fun getExistingPath(audio: Audio): String? {
        val file = getFile(audio)
        return if (file.exists()) {
            file.absolutePath
        } else {
            null
        }
    }

    fun downloadTrack(track: Track, callback: (Track) -> Unit) {
        if (track.isCached()) return
        val url = track.audio.url ?: return

        val trackPath = getFile(track.audio).absolutePath
        //TODO add newer download service if needed
    }

    fun getTrack(audio: Audio) = Track(audio, getExistingPath(audio))

    fun getTracks(audios: List<Audio>) = ArrayList(audios.map { getTrack(it) })

    fun removeTrack(track: Track) {
        getFile(track.audio).delete()
    }

    companion object {
        const val TRACK_DIR = "tracks"
    }

}