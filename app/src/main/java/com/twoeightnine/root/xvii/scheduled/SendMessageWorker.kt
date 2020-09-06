package com.twoeightnine.root.xvii.scheduled

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.background.messaging.MessageDestructionService
import com.twoeightnine.root.xvii.db.AppDb
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.network.ApiService
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
        createChannel()

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
                        var peer = dialog.alias ?: dialog.title
                        if (Prefs.lowerTexts) {
                            peer = peer.toLowerCase()
                        }
                        showNotification(success, peerId, peer)
                    }, {
                        showNotification(success, peerId, "id$peerId")
                    })
        }
    }

    private fun showNotification(success: Boolean, notificationId: Int, peer: String? = null) {
        val notification = NotificationCompat.Builder(applicationContext, MessageDestructionService.CHANNEL_ID)
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

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {

            val name = applicationContext.getString(R.string.scheduled_messages_name)
            val channel = NotificationChannel(CHANNEL_ID, name,
                    NotificationManager.IMPORTANCE_LOW
            ).apply {
                enableVibration(false)
                setSound(null, null)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {

        const val NOTIFICATION_ID = Int.MAX_VALUE
        const val CHANNEL_ID = "xvii.scheduled_messages"
        const val TAG = "[send worker]"

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
        }

        fun cancelWorker(context: Context, scheduledMessageId: Int) {
            WorkManager.getInstance(context)
                    .cancelAllWorkByTag(getWorkerTag(scheduledMessageId))
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
    }
}