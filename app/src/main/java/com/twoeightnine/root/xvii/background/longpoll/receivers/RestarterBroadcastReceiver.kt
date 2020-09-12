package com.twoeightnine.root.xvii.background.longpoll.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.twoeightnine.root.xvii.background.longpoll.LongPollCore
import com.twoeightnine.root.xvii.lg.L
import com.twoeightnine.root.xvii.utils.startNotificationService

/**
 * Created by msnthrp on 14/01/18.
 */

class RestarterBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        if ((action == RESTART_ACTION || action == Intent.ACTION_BOOT_COMPLETED)
                && context != null
                && !LongPollCore.isRunning()) {
            L.tag(TAG).log("starting service")
            startNotificationService(context)
        }
    }

    companion object {
        private const val TAG = "restarter"
        const val RESTART_ACTION = "restartAction"
    }
}