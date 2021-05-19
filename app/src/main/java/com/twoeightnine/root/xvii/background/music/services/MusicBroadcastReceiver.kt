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

package com.twoeightnine.root.xvii.background.music.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MusicBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action ?: return) {
            ACTION_NEXT -> MusicService.next()
            ACTION_PREVIOUS -> MusicService.previous()
            ACTION_PLAY_PAUSE -> MusicService.playPause()
            ACTION_SPEED -> MusicService.toggleSpeed()
            ACTION_CLOSE -> MusicService.exit()
        }
    }

    companion object {
        const val ACTION_NEXT = "actionNext"
        const val ACTION_PREVIOUS = "actionPrev"
        const val ACTION_PLAY_PAUSE = "actionPlayPause"
        const val ACTION_SPEED = "actionSpeed"
        const val ACTION_CLOSE = "actionClose"
    }
}