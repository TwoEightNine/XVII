package com.twoeightnine.root.xvii.background.longpoll.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.lg.L
import com.twoeightnine.root.xvii.utils.ApiUtils
import javax.inject.Inject

class MarkAsReadBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var apiUtils: ApiUtils

    override fun onReceive(context: Context?, intent: Intent?) {
        intent ?: return
        try {
            App.appComponent?.inject(this)
            val action = intent.action ?: return
            val messageId = intent.extras?.getInt(ARG_MESSAGE_ID) ?: return
            val peerId = intent.extras?.getInt(ARG_PEER_ID) ?: return

            L.tag(TAG).log("received $action with message $messageId for peer $peerId")

            if (action == ACTION_MARK_AS_READ) {
                apiUtils.markAsRead("$messageId")
                (context?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)
                        ?.cancel(peerId)
            }
        } catch (e: Exception) {
            L.tag(TAG).throwable(e).log("unable to mark message\ndata: ${intent.extras}")
        }
    }

    companion object {

        private const val TAG = "mark as read"

        const val ACTION_MARK_AS_READ = "markAsReadAction"
        const val ARG_MESSAGE_ID = "messageId"
        const val ARG_PEER_ID = "peerId"
    }
}