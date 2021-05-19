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

package com.twoeightnine.root.xvii.scheduled.core

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.lg.L
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.utils.NotificationChannels
import global.msnthrp.xvii.data.db.AppDb
import global.msnthrp.xvii.data.scheduled.ScheduledMessage
import global.msnthrp.xvii.uikit.extensions.lowerIf
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.random.Random

class SendMessageWorker(
        context: Context,
        workerParameters: WorkerParameters
) : Worker(context, workerParameters) {

    private val notificationManager by lazy {
        applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
    }

    @Inject
    lateinit var api: ApiService

    @Inject
    lateinit var appDb: AppDb

    override fun doWork(): Result {
        val peerId = inputData.getInt(ARG_MESSAGE_PEER_ID, 0)
        val text = inputData.getString(ARG_MESSAGE_TEXT)
        val attachments = inputData.getString(ARG_MESSAGE_ATTACHMENTS)
        val fwdMessages = inputData.getString(ARG_MESSAGE_FWD_MESSAGES)
        if (peerId == 0 || text == null) {
            showNotificationForPeer(success = false)
            lw("received peerId = $peerId, text = $text")
            return Result.failure()
        }

        App.appComponent?.inject(this)
        val response = api.sendMessage(
                peerId = peerId,
                randomId = Random.nextInt(),
                text = text,
                forwardedMessages = fwdMessages,
                attachments = attachments
        ).blockingFirst()

        l("message sent: $response")
        return if (response.response != null) {
            showNotificationForPeer(peerId = peerId, success = true)
            Result.success()
        } else {
            showNotificationForPeer(peerId = peerId, success = false)
            Result.retry()
        }
    }

    @SuppressLint("CheckResult")
    private fun showNotificationForPeer(success: Boolean, peerId: Int = 0) {
        if (peerId == 0) {
            showNotification(success, notificationId = NOTIFICATION_ID, peer = null)
        } else {
            appDb.dialogsDao()
                    .getDialogs(peerId)
                    .subscribe({ dialog ->
                        val peer = dialog.aliasOrTitle.lowerIf(Prefs.lowerTexts)
                        showNotification(success, peerId, peer)
                    }, { throwable ->
                        lw("error fetching peer id", throwable)
                        showNotification(success, peerId, "id$peerId")
                    })
        }
    }

    private fun showNotification(success: Boolean, notificationId: Int, peer: String? = null) {
        val notification = NotificationCompat.Builder(applicationContext, NotificationChannels.scheduledMessages.id)
                .setContentTitle(peer)
                .setContentText(applicationContext.getString(if (success) {
                    R.string.scheduled_hint_success
                } else {
                    R.string.scheduled_hint_failure
                }))
                .setSmallIcon(R.drawable.ic_clock)
                .setWhen(System.currentTimeMillis())
                .build()
        notificationManager.notify(notificationId, notification)
    }

    companion object {

        private const val TAG = "message scheduler"

        const val NOTIFICATION_ID = Int.MAX_VALUE

        const val ARG_MESSAGE_ID = "scheduledMessage_id"
        const val ARG_MESSAGE_PEER_ID = "scheduledMessage_peerId"
        const val ARG_MESSAGE_WHEN = "scheduledMessage_whenMs"
        const val ARG_MESSAGE_TEXT = "scheduledMessage_text"
        const val ARG_MESSAGE_ATTACHMENTS = "scheduledMessage_attachments"
        const val ARG_MESSAGE_FWD_MESSAGES = "scheduledMessage_forwardedMessages"

        fun enqueueWorker(context: Context, scheduledMessage: ScheduledMessage) {
            val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()

            val request = OneTimeWorkRequestBuilder<SendMessageWorker>()
                    .setConstraints(constraints)
                    .setInitialDelay(scheduledMessage.whenMs - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                    .setInputData(createInputData(scheduledMessage))
                    .addTag(getWorkerTag(scheduledMessage.id))
                    .build()

            WorkManager.getInstance(context)
                    .enqueue(request)
            l("message added: ${scheduledMessage.id}")
        }

        fun cancelWorker(context: Context, scheduledMessageId: Int) {
            WorkManager.getInstance(context)
                    .cancelAllWorkByTag(getWorkerTag(scheduledMessageId))
            l("message cancelled: $scheduledMessageId")
        }

        private fun getWorkerTag(scheduledMessageId: Int) =
                "send_${scheduledMessageId}"

        private fun createInputData(scheduledMessage: ScheduledMessage) = workDataOf(
                ARG_MESSAGE_ID to scheduledMessage.id,
                ARG_MESSAGE_PEER_ID to scheduledMessage.peerId,
                ARG_MESSAGE_WHEN to scheduledMessage.whenMs,
                ARG_MESSAGE_TEXT to scheduledMessage.text,
                ARG_MESSAGE_ATTACHMENTS to scheduledMessage.attachments,
                ARG_MESSAGE_FWD_MESSAGES to scheduledMessage.forwardedMessages
        )

        private fun l(s: String) {
            L.tag(TAG).log(s)
        }

        private fun lw(s: String, throwable: Throwable? = null) {
            L.tag(TAG)
                    .throwable(throwable)
                    .log(s)
        }

    }
}