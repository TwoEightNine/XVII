package com.twoeightnine.root.xvii.utils

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import androidx.annotation.StringRes
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.managers.Prefs

@TargetApi(26)
object NotificationChannels {

    private val RING_URI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    private val VIBRATE_PATTERN = longArrayOf(0L, 200L)

    val privateMessages = Channel(
            id = "xvii.private_messages",
            name = R.string.channel_private_messages,
            description = 0,
            importance = NotificationManager.IMPORTANCE_DEFAULT,
            sound = Prefs.sound,
            vibrate = Prefs.vibrate,
            isNewMessages = true
    )

    val otherMessages = Channel(
            id = "xvii.other_messages",
            name = R.string.channel_other_messages,
            description = 0,
            importance = NotificationManager.IMPORTANCE_DEFAULT,
            sound = Prefs.soundChats,
            vibrate = Prefs.vibrateChats,
            isNewMessages = true
    )

    val backgroundService = Channel(
            id = "xvii.background_service",
            name = R.string.channel_background_service,
            description = 0,
            importance = NotificationManager.IMPORTANCE_LOW,
            sound = false,
            vibrate = false
    )

    val musicPlayer = Channel(
            id = "xvii.player",
            name = R.string.channel_player,
            description = 0,
            importance = NotificationManager.IMPORTANCE_LOW,
            sound = false,
            vibrate = false
    )

    val messageDestructor = Channel(
            id = "xvii.self_destructing_messages",
            name = R.string.channel_message_destructor,
            description = 0,
            importance = NotificationManager.IMPORTANCE_LOW,
            sound = false,
            vibrate = false
    )

    val scheduledMessages = Channel(
            id = "xvii.postponed_messages",
            name = R.string.channel_scheduled_messages,
            description = 0,
            importance = NotificationManager.IMPORTANCE_LOW,
            sound = false,
            vibrate = false
    )

    val keyExchanges = Channel(
            id = "xvii.key_exchanges",
            name = R.string.channel_key_exchanges,
            description = 0,
            importance = NotificationManager.IMPORTANCE_DEFAULT,
            sound = false,
            vibrate = true
    )

    private val newMessages = Group(
            id = "xvii.group.new_messages",
            name = R.string.channel_new_messages
    )

    private val other = Group(
            id = "xvii.group.other",
            name = R.string.channel_other
    )

    private val channels = listOf(
            privateMessages,
            otherMessages,
            backgroundService,
            musicPlayer,
            messageDestructor,
            scheduledMessages
    )

    private val groups = listOf(newMessages, other)

    private val oldNotificationChannels = listOf(
            "xvii.messages",
            "xvii.notifications",
            "xvii.service",
            "xvii.message_destructor",
            "xvii.scheduled_messages",
            "xvii.music"
    )

    fun initChannels(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        oldNotificationChannels.forEach { channelId ->
            notificationManager.deleteNotificationChannel(channelId)
        }

        groups.forEach { group ->
            val name = context.getString(group.name)
            NotificationChannelGroup(group.id, name).apply {
                notificationManager.createNotificationChannelGroup(this)
            }
        }

        channels.forEach { channel ->
            val name = context.getString(channel.name)
            NotificationChannel(channel.id, name, channel.importance).apply {
                vibrationPattern = when {
                    channel.vibrate -> VIBRATE_PATTERN
                    else -> null
                }
                enableVibration(channel.vibrate)
                setSound(when {
                    channel.sound -> RING_URI
                    else -> null
                }, null)
                group = if (channel.isNewMessages) {
                    newMessages.id
                } else {
                    other.id
                }
                notificationManager.createNotificationChannel(this)
            }
        }
    }

    data class Group(
            val id: String,

            @StringRes
            val name: Int
    )

    data class Channel(
            val id: String,

            @StringRes
            val name: Int,

            @StringRes
            val description: Int,

            val importance: Int,

            val sound: Boolean,

            val vibrate: Boolean,

            val isNewMessages: Boolean = false
    )
}