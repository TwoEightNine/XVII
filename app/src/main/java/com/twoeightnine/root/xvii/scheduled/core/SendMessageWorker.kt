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
        val scheduledMessageId = inputData.getInt(ARG_MESSAGE_ID, NO_ID)

        if (scheduledMessageId == NO_ID) {
            showNotificationForPeer(success = false)
            lw("received scheduledMessageId = -1")
            return Result.failure()
        }

        App.appComponent?.inject(this)
        val scheduledMessage = appDb.scheduledMessagesDao()
                .getScheduledMessage(scheduledMessageId)
                .blockingGet()

        if (scheduledMessage.peerId == 0) {
            showNotificationForPeer(success = false)
            lw("received $scheduledMessage")
            return Result.failure()
        }

        val response = api.sendMessage(
                peerId = scheduledMessage.peerId,
                randomId = Random.nextInt(),
                text = scheduledMessage.text,
                forwardedMessages = scheduledMessage.forwardedMessages,
                attachments = scheduledMessage.attachments
        ).blockingFirst()

        l("message sent: $response")
        return if (response.response != null) {
            showNotificationForPeer(peerId = scheduledMessage.peerId, success = true)
            Result.success()
        } else {
            showNotificationForPeer(peerId = scheduledMessage.peerId, success = false)
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
        private const val NO_ID = -1

        const val ARG_MESSAGE_ID = "scheduledMessage_id"

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
                ARG_MESSAGE_ID to scheduledMessage.id
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