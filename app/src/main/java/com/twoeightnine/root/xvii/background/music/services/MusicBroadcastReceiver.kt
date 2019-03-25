package com.twoeightnine.root.xvii.background.music.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MusicBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return
        when (action) {
            ACTION_NEXT -> MusicService.next()
            ACTION_PREVIOUS -> MusicService.previous()
            ACTION_PLAY_PAUSE -> MusicService.playPause()
            ACTION_CLOSE -> MusicService.exit()
        }
    }

    companion object {
        const val ACTION_NEXT = "actionNext"
        const val ACTION_PREVIOUS = "actionPrev"
        const val ACTION_PLAY_PAUSE = "actionPlayPause"
        const val ACTION_CLOSE = "actionClose"
    }
}