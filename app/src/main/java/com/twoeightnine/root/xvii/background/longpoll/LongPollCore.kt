package com.twoeightnine.root.xvii.background.longpoll

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Html
import androidx.core.app.NotificationCompat
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.activities.RootActivity
import com.twoeightnine.root.xvii.background.longpoll.models.LongPollServer
import com.twoeightnine.root.xvii.background.longpoll.models.LongPollUpdate
import com.twoeightnine.root.xvii.background.longpoll.models.events.LongPollEventFactory
import com.twoeightnine.root.xvii.background.longpoll.models.events.NewMessageEvent
import com.twoeightnine.root.xvii.background.longpoll.models.events.UnreadCountEvent
import com.twoeightnine.root.xvii.background.longpoll.receivers.MarkAsReadBroadcastReceiver
import com.twoeightnine.root.xvii.dagger.ApiService
import com.twoeightnine.root.xvii.lg.Lg
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.model.UserDb
import com.twoeightnine.root.xvii.utils.*
import io.reactivex.disposables.CompositeDisposable
import io.realm.Realm
import javax.inject.Inject

class LongPollCore(private val context: Context) {

    private val vibrator by lazy { context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }
    private val ringtone by lazy {
        RingtoneManager.getRingtone(
                context,
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        )
    }
    private val notificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private val disposables = CompositeDisposable()
    private var isRunning = false
    private var lastCount = 0

    @Inject
    lateinit var longPollStorage: LongPollStorage

    @Inject
    lateinit var api: ApiService

    init {
        App.appComponent?.inject(this)
    }

    fun run(intent: Intent?) {
        while (true) {
            while (isRunning) {
                Thread.sleep(WAIT_DELAY)
            }
            getUpdates()
        }
    }

    private fun getUpdates() {
        isRunning = true
        lastRun = time()
        longPollStorage.getLongPollServer() ?: updateLongPollServer()

        l("on ${longPollStorage.getLongPollServer()?.ts}")
        getConnectSingle(longPollStorage.getLongPollServer() ?: return)
                .subscribe({ longPollUpdate: LongPollUpdate ->
                    onUpdateReceived(longPollUpdate)
                    isRunning = false
                }, {
                    lw("error ${it.message} during getting updates")
                    Thread.sleep(NO_NETWORK_DELAY)
                    isRunning = false
                }).let { disposables.add(it) }
    }

    private fun updateLongPollServer() {
        api.getLongPollServer()
                .subscribeSmart({
                    longPollStorage.saveLongPoll(it)
                    isRunning = false
                }, { msg ->
                    lw("error during updating: $msg")
                    Thread.sleep(NO_NETWORK_DELAY)
                    isRunning = false
                }, {
                    lw("no network")
                    Thread.sleep(NO_NETWORK_DELAY)
                    isRunning = false
                }).let { disposables.add(it) }
    }

    private fun onUpdateReceived(longPollUpdate: LongPollUpdate) {
        when {
            longPollUpdate.shouldUpdateServer() -> updateLongPollServer()
            else -> {
                updateTs(longPollStorage.getLongPollServer() ?: return, longPollUpdate.ts)
                deliverUpdate(longPollUpdate.updates)
            }
        }
    }

    private fun deliverUpdate(updates: ArrayList<ArrayList<Any>>) {
        val events = LongPollEventFactory.createAll(updates)
        if (events.isNotEmpty()) {
            l("updates: ${events.size}")
        }
        events.forEach { event ->
            EventBus.publishLongPollEventReceived(event)

            when (event) {
                is UnreadCountEvent -> processUnreadCount(event)
                is NewMessageEvent -> processNewMessage(event)
            }
        }
    }

    private fun processUnreadCount(event: UnreadCountEvent) {
        val decrease = lastCount > event.unreadCount
        lastCount = event.unreadCount
        if (decrease) {
            notificationManager.cancelAll()
        }
    }

    private fun processNewMessage(event: NewMessageEvent) {
        if (event.isOut() || !Prefs.showNotifs
                || event.peerId in Prefs.muteList) return

        if (Prefs.showNotifsChats || event.isUser()) {

            if (!isInForeground()) {
                if (Prefs.vibrate) {
                    vibrate()
                }
                if (Prefs.sound) {
                    ringtone.play()
                }
            }
        }

        val content = if (Prefs.showContent) {
            event.text
        } else {
            context.getString(R.string.hidden_message)
        }

        if (event.isUser()) {
            val user = getUser(event.peerId)
            if (user != null) {
                if (Prefs.showName) {
                    loadBitmapIcon(user.photo100) { bitmap ->
                        showNotification(
                                content,
                                event.peerId,
                                event.id,
                                user.fullName,
                                user.fullName,
                                bitmap
                        )
                    }
                } else {
                    showNotification(content, event.peerId, event.id, user.fullName)
                }
            } else {
                showNotification(content, event.peerId, event.id)
            }
        } else {
            showNotification(content, event.peerId, event.id, event.title)
        }

    }

    fun onDestroy() {
        disposables.dispose()
    }

    private fun updateTs(longPollServer: LongPollServer, ts: Int) {
        val newServer = LongPollServer(
                longPollServer.key,
                longPollServer.server,
                ts
        )
        longPollStorage.saveLongPoll(newServer)
    }

    private fun showNotification(
            content: String,
            peerId: Int,
            messageId: Int,
            userName: String = context.getString(R.string.app_name),
            title: String = context.getString(R.string.app_name),
            icon: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.xvii128)
    ) {

        createNotificationChannel()

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setLargeIcon(icon)
                .setSmallIcon(com.twoeightnine.root.xvii.R.drawable.ic_message)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setContentText(Html.fromHtml(content))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .addAction(
                        R.drawable.ic_eye,
                        context.getString(R.string.mark_as_read),
                        getMarkAsReadIntent(messageId, peerId)
                )
                .setContentIntent(getOpenAppIntent(peerId, userName))

        if (Prefs.ledLights) {
            builder.setLights(Prefs.color, 500, 500)
        }

        notificationManager.notify(peerId, builder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.app_name)
            val descriptionText = context.getString(R.string.app_name)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = descriptionText
            channel.setSound(null, null)

            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getOpenAppIntent(peerId: Int, userName: String): PendingIntent {
        val openAppIntent = Intent(context, RootActivity::class.java).apply {
            putExtra(RootActivity.USER_ID, peerId)
            putExtra(RootActivity.TITLE, userName)
        }
        return PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun getMarkAsReadIntent(messageId: Int, peerId: Int): PendingIntent {
        val markAsReadIntent = Intent(context, MarkAsReadBroadcastReceiver::class.java).apply {
            action = MarkAsReadBroadcastReceiver.ACTION_MARK_AS_READ
            putExtra(MarkAsReadBroadcastReceiver.ARG_MESSAGE_ID, messageId)
            putExtra(MarkAsReadBroadcastReceiver.ARG_PEER_ID, peerId)
        }
        return PendingIntent.getBroadcast(
                context,
                0,
                markAsReadIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun getUser(peerId: Int): User? {
        try {
            val realm = Realm.getDefaultInstance()
            val realmData = realm
                    .where(UserDb::class.java)
                    .equalTo("id", peerId)
            val realmUser = realmData.findFirst()
            return User(realmUser)
        } catch (e: Exception) {
            lw("get user error: ${e.message}")
            return null
        }
    }

    private fun vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(VIBRATE_DELAY, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(VIBRATE_DELAY)
        }
    }

    private fun getConnectSingle(longPollServer: LongPollServer) = api.connectLongPoll("https://${longPollServer.server}", longPollServer.key, longPollServer.ts)

    private fun l(s: String) {
        Lg.i("[longpoll] $s")
    }

    private fun lw(s: String) {
        Lg.wtf("[longpoll] $s")
    }

    companion object {

        private const val CHANNEL_ID = "xvii.notifications"

        private const val VIBRATE_DELAY = 60L
        private const val WAIT_DELAY = 1000L
        private const val NO_NETWORK_DELAY = 5000L

        private const val LAST_RUN_ALLOWED_DELAY = 1000L * 45

        /**
         * watches for running
         */
        var lastRun = 0
            private set

        fun isRunning() = time() - lastRun < LAST_RUN_ALLOWED_DELAY
    }
}