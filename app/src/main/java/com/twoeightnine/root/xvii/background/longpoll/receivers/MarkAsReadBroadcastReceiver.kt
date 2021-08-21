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

package com.twoeightnine.root.xvii.background.longpoll.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.lg.L
import com.twoeightnine.root.xvii.utils.ApiUtils
import com.twoeightnine.root.xvii.utils.notifications.NotificationUtils
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
                apiUtils.markAsRead(peerId)
                context?.also {
                    NotificationUtils.hideMessageNotification(context, peerId)
                }
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