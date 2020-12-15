package com.twoeightnine.root.xvii.utils.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.text.Html
import android.widget.RemoteViews
import androidx.annotation.LayoutRes
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.background.longpoll.LongPollExplanationActivity
import com.twoeightnine.root.xvii.background.longpoll.receivers.MarkAsReadBroadcastReceiver
import com.twoeightnine.root.xvii.background.music.services.MusicBroadcastReceiver
import com.twoeightnine.root.xvii.main.MainActivity
import com.twoeightnine.root.xvii.model.attachments.Audio
import com.twoeightnine.root.xvii.utils.BitmapNotification
import com.twoeightnine.root.xvii.utils.NotificationChannels
import com.twoeightnine.root.xvii.utils.getTime
import com.twoeightnine.root.xvii.utils.matchesUserId
import kotlin.random.Random

object NotificationUtils {

    private val RING_URI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    private val VIBRATE_PATTERN = longArrayOf(0L, 200L)

    fun showLongPollNotification(service: Service) {
        val explainIntent = Intent(service, LongPollExplanationActivity::class.java)
        val explainPendingIntent = PendingIntent.getActivity(service, 0, explainIntent, 0)
        val notification = NotificationCompat.Builder(service, NotificationChannels.backgroundService.id)
                .setContentIntent(explainPendingIntent)
                .setShowWhen(false)
                .setOngoing(true)
                .setVibrate(null)
                .setSound(null)
                .setSmallIcon(R.drawable.shape_transparent)
                .setContentTitle(service.getString(R.string.xvii_longpoll))
                .setContentText(service.getString(R.string.longpoll_hint))
                .build()
        service.startForeground(9999, notification)
    }

    fun showMusicNotification(audio: Audio, service: Service, isPlaying: Boolean, playbackSpeed: Float) {
        val notification = NotificationCompat.Builder(service.applicationContext, NotificationChannels.musicPlayer.id)
                .setCustomContentView(bindMusicRemoteViews(service, R.layout.view_music_notification, audio, isPlaying, playbackSpeed))
                .setCustomBigContentView(bindMusicRemoteViews(service, R.layout.view_music_notification_extended, audio, isPlaying, playbackSpeed, true))
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setSmallIcon(R.drawable.ic_play_filled)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setColor(ContextCompat.getColor(service.applicationContext, R.color.background))
                .build()

        service.startForeground(3676, notification)
    }

    fun showNewMessageNotification(
            context: Context,
            content: ArrayList<String>,
            timeStamp: Long,
            peerId: Int,
            messageId: Int,
            userName: String,
            title: String,
            icon: Bitmap,
            ledColor: Int,
            photo: String?,
            unreadMessagesCount: Int,
            shouldVibrate: Boolean,
            shouldRing: Boolean,
            stylish: Boolean,
            isPeerIdStillActual: (Int) -> Boolean
    ) {

        if (content.isEmpty()) {
            content.add(context.getString(R.string.messages))
        }
        val text = Html.fromHtml(content.last())
        val textBig = Html.fromHtml(content.joinToString(separator = "<br>"))

        val channelId = when {
            peerId.matchesUserId() -> NotificationChannels.privateMessages.id
            else -> NotificationChannels.otherMessages.id
        }

        if (stylish) {
            BitmapNotification.load(context, icon) { notificationBackground ->
                val builder = NotificationCompat.Builder(context, channelId)
                        .setCustomContentView(
                                getNotificationView(
                                        context,
                                        R.layout.view_notification_message,
                                        notificationBackground,
                                        title, text, (timeStamp / 1000).toInt()
                                )
                        )
                        .setCustomBigContentView(
                                getNotificationView(
                                        context,
                                        R.layout.view_notification_message_extended,
                                        notificationBackground,
                                        title, textBig, (timeStamp / 1000).toInt(),
                                        getMarkAsReadIntent(context, messageId, peerId)
                                )
                        )
                        .setContentText(text)
                        .setContentTitle(title)
                endUpShowingNotification(
                        context,
                        builder, peerId, timeStamp, userName,
                        shouldVibrate, shouldRing, ledColor, photo,
                        isPeerIdStillActual
                )
            }
        } else {
            val builder = NotificationCompat.Builder(context, channelId)
                    .setLargeIcon(icon)
                    .setContentTitle(title)
                    .setAutoCancel(true)
                    .setWhen(timeStamp)
                    .setContentText(text)
                    .setNumber(unreadMessagesCount)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(textBig))
                    .addAction(
                            R.drawable.ic_eye,
                            context.getString(R.string.mark_as_read),
                            getMarkAsReadIntent(context, messageId, peerId)
                    )
            endUpShowingNotification(
                    context,
                    builder, peerId, timeStamp, userName,
                    shouldVibrate, shouldRing, ledColor, photo,
                    isPeerIdStillActual
            )
        }
    }

    private fun endUpShowingNotification(
            context: Context,
            builder: NotificationCompat.Builder,
            peerId: Int,
            timeStamp: Long,
            userName: String,
            shouldVibrate: Boolean,
            shouldRing: Boolean,
            ledColor: Int,
            photo: String? = null,
            isPeerIdStillActual: (Int) -> Boolean
    ) {
        builder.setSmallIcon(R.drawable.ic_envelope)
                .setAutoCancel(true)
                .setWhen(timeStamp)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setContentIntent(getOpenAppIntent(context, peerId, userName, photo))
        if (ledColor != Color.BLACK) {
            builder.setLights(ledColor, 500, 500)
        }

        val notification = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            if (shouldRing) {
                builder.setSound(RING_URI)
            }
            if (shouldVibrate) {
                builder.setVibrate(VIBRATE_PATTERN)
            }

            val notification = builder.build()
            notification.defaults = notification.defaults or
                    if (shouldRing) Notification.DEFAULT_SOUND else 0 or
                            if (shouldVibrate) Notification.DEFAULT_VIBRATE else 0
            notification
        } else {
            builder.build()
        }

        if (isPeerIdStillActual(peerId)) {
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                    .notify(peerId, notification)
        }
    }

    private fun getNotificationView(
            context: Context,
            @LayoutRes layoutId: Int,
            notificationBackground: BitmapNotification.NotificationBackground,
            name: String,
            message: CharSequence,
            timeStamp: Int,
            onClickPendingIntent: PendingIntent? = null
    ) = RemoteViews(context.packageName, layoutId).apply {

        setTextViewText(R.id.tvName, name)
        setTextViewText(R.id.tvMessages, message)
        setTextViewText(R.id.tvWhen, getTime(timeStamp, shortened = true))

        setImageViewBitmap(R.id.ivBack, notificationBackground.background)
        setInt(R.id.rlBack, "setBackgroundColor", notificationBackground.backgroundColor)

        setTextColor(R.id.tvName, notificationBackground.textColor)
        setTextColor(R.id.tvMessages, notificationBackground.textColor)
        setTextColor(R.id.tvWhen, notificationBackground.textColor)
        setTextColor(R.id.tvAppName, notificationBackground.textColor)

        setInt(R.id.ivMessageIcon, "setColorFilter", notificationBackground.textColor)

        onClickPendingIntent?.also {
            setOnClickPendingIntent(R.id.tvMarkAsRead, it)
            setTextColor(R.id.tvMarkAsRead, notificationBackground.textColor)
        }
    }

    private fun bindMusicRemoteViews(
            service: Service,
            @LayoutRes viewId: Int,
            audio: Audio,
            isPlaying: Boolean,
            playbackSpeed: Float,
            isExtended: Boolean = false
    ): RemoteViews {
        val remoteViews = RemoteViews(service.packageName, viewId)
        with(remoteViews) {
            setTextViewText(R.id.tvTitle, audio.title)
            setTextViewText(R.id.tvArtist, audio.artist)

            val playPauseRes = if (isPlaying) R.drawable.ic_pause_filled else R.drawable.ic_play_filled
            setImageViewResource(R.id.ivPlayPause, playPauseRes)
            setOnClickPendingIntent(R.id.ivNext, getMusicActionPendingIntent(service.applicationContext, MusicBroadcastReceiver.ACTION_NEXT))
            setOnClickPendingIntent(R.id.ivPrevious, getMusicActionPendingIntent(service.applicationContext, MusicBroadcastReceiver.ACTION_PREVIOUS))
            setOnClickPendingIntent(R.id.ivPlayPause, getMusicActionPendingIntent(service.applicationContext, MusicBroadcastReceiver.ACTION_PLAY_PAUSE))

            if (isExtended) {
                setOnClickPendingIntent(R.id.tvClose, getMusicActionPendingIntent(service.applicationContext, MusicBroadcastReceiver.ACTION_CLOSE))
                setOnClickPendingIntent(R.id.tvPlaybackSpeed, getMusicActionPendingIntent(service.applicationContext, MusicBroadcastReceiver.ACTION_SPEED))

                setTextViewText(R.id.tvPlaybackSpeed, service.getString(R.string.playback_speed, playbackSpeed.toString()))
            }
        }
        return remoteViews
    }

    private fun getMusicActionPendingIntent(applicationContext: Context, action: String) = PendingIntent.getBroadcast(
            applicationContext, Random.nextInt(),
            Intent(applicationContext, MusicBroadcastReceiver::class.java).apply {
                this.action = action
            },
            PendingIntent.FLAG_UPDATE_CURRENT
    )

    private fun getOpenAppIntent(context: Context, peerId: Int, userName: String, photo: String?): PendingIntent {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            putExtra(MainActivity.USER_ID, peerId)
            putExtra(MainActivity.TITLE, userName)
            putExtra(MainActivity.PHOTO, photo)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun getMarkAsReadIntent(context: Context, messageId: Int, peerId: Int): PendingIntent {
        val markAsReadIntent = Intent(context, MarkAsReadBroadcastReceiver::class.java).apply {
            action = MarkAsReadBroadcastReceiver.ACTION_MARK_AS_READ
            putExtra(MarkAsReadBroadcastReceiver.ARG_MESSAGE_ID, messageId)
            putExtra(MarkAsReadBroadcastReceiver.ARG_PEER_ID, peerId)
        }
        return PendingIntent.getBroadcast(
                context,
                messageId,
                markAsReadIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

}