package com.twoeightnine.root.xvii.background.longpoll

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Html
import androidx.core.app.NotificationCompat
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.background.longpoll.models.LongPollServer
import com.twoeightnine.root.xvii.background.longpoll.models.LongPollUpdate
import com.twoeightnine.root.xvii.background.longpoll.models.events.*
import com.twoeightnine.root.xvii.background.longpoll.receivers.MarkAsReadBroadcastReceiver
import com.twoeightnine.root.xvii.db.AppDb
import com.twoeightnine.root.xvii.dialogs.models.Dialog
import com.twoeightnine.root.xvii.lg.Lg
import com.twoeightnine.root.xvii.main.MainActivity
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.utils.*
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class LongPollCore(private val context: Context) {

    private val vibrator by lazy { context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }
    private val notificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private val unreadMessages = hashMapOf<Int, ArrayList<String>>()
    private val disposables = CompositeDisposable()
    private var isRunning = false

    @Inject
    lateinit var longPollStorage: LongPollStorage

    @Inject
    lateinit var api: ApiService

    @Inject
    lateinit var appDb: AppDb

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
                }, {
                    lw("error ${it.message} during getting updates")
                    waitInBg(NO_NETWORK_DELAY)
                }).let { disposables.add(it) }
    }

    private fun updateLongPollServer() {
        api.getLongPollServer()
                .subscribeSmart({
                    longPollStorage.saveLongPoll(it)
                    isRunning = false
                }, { msg ->
                    lw("error during updating: $msg")
                    waitInBg(NO_NETWORK_DELAY)
                }, {
                    lw("no network")
                    waitInBg(NO_NETWORK_DELAY)
                }).let { disposables.add(it) }
    }

    private fun onUpdateReceived(longPollUpdate: LongPollUpdate) {
        when {
            longPollUpdate.shouldUpdateServer() -> updateLongPollServer()
            else -> {
                updateTs(longPollStorage.getLongPollServer() ?: return, longPollUpdate.ts)
                deliverUpdate(longPollUpdate.updates)
                isRunning = false
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
                is ReadIncomingEvent -> processReadIncoming(event)
                is NewMessageEvent -> processNewMessage(event)
            }
        }
    }

    private fun processReadIncoming(event: ReadIncomingEvent) {
        unreadMessages[event.peerId]?.clear()
        notificationManager.cancel(event.peerId)
    }

    private fun processUnreadCount(event: UnreadCountEvent) {
        if (event.unreadCount == 0) {
            unreadMessages.clear()
            notificationManager.cancelAll()
        }
    }

    private fun processNewMessage(event: NewMessageEvent) {
        if (event.isOut()
                || event.peerId in Prefs.muteList
                || event.isUser() && !Prefs.showNotifs
                || !event.isUser() && !Prefs.showNotifsChats) return

        val shouldVibrate = Prefs.vibrateChats && !event.isUser()
                || Prefs.vibrate && event.isUser()
        if (!isInForeground()) {
            if (shouldVibrate) vibrate()
        }

        val shouldShowContent = event.isUser() && Prefs.showContent
                || !event.isUser() && Prefs.showContentChats
        if (event.peerId !in unreadMessages) {
            unreadMessages[event.peerId] = arrayListOf()
        }
        unreadMessages[event.peerId]?.add(event.getResolvedMessage(context, !shouldShowContent))

        val content = if (shouldShowContent) {
            unreadMessages[event.peerId]
                    ?: arrayListOf(context.getString(R.string.messages))
        } else {
            val count = unreadMessages[event.peerId]?.size ?: 0
            arrayListOf(context.resources.getQuantityString(R.plurals.messages, count, count))
        }
        val timeStamp = event.timeStamp * 1000L
        val ledColor = if (event.isUser()) Prefs.ledColor else Prefs.ledColorChats

        // trying to get dialog from database
        getDialog(event.peerId, { dialog ->
            if (Prefs.showName) {
                loadBitmapIcon(dialog.photo) { bitmap ->
                    val name = dialog.alias ?: dialog.title
                    showNotification(
                            content,
                            timeStamp,
                            event.peerId,
                            event.id,
                            name,
                            name,
                            bitmap,
                            ledColor,
                            dialog.photo
                    )
                }
            } else {
                showNotification(content, timeStamp, event.peerId, event.id,
                        dialog.title, ledColor = ledColor, photo = dialog.photo)
            }
        }, {
            if (event.peerId.matchesChatId()) {
                // chats are shown as is
                showNotification(content, timeStamp, event.peerId, event.id,
                        event.title, ledColor = ledColor)
            } else {

                // for groups and users try to resolve them
                resolveSenderByPeerId(event.peerId, { title, photo ->
                    loadBitmapIcon(photo) { bitmap ->
                        showNotification(
                                content,
                                timeStamp,
                                event.peerId,
                                event.id,
                                title,
                                title,
                                bitmap,
                                ledColor,
                                photo
                        )
                    }
                }, {
                    showNotification(content, timeStamp, event.peerId, event.id, event.title, ledColor = ledColor)
                })
            }
        })

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
            content: ArrayList<String>,
            timeStamp: Long,
            peerId: Int,
            messageId: Int,
            userName: String = context.getString(R.string.app_name),
            title: String = context.getString(R.string.app_name),
            icon: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.xvii_dark_logo_128),
            ledColor: Int = Color.BLACK,
            photo: String? = null
    ) {
        val shouldVibrate = Prefs.vibrateChats && !peerId.matchesUserId()
                || Prefs.vibrate && peerId.matchesUserId()
        val shouldRing = Prefs.soundChats && !peerId.matchesUserId()
                || Prefs.sound && peerId.matchesUserId()

        createNotificationChannel(shouldRing, shouldVibrate)

        if (content.isEmpty()) {
            content.add(context.getString(R.string.messages))
        }
        val text = Html.fromHtml(content.last())
        val textBig = Html.fromHtml(content.joinToString(separator = "<br>"))

        val builder = NotificationCompat.Builder(context, NOTIFICATIONS_CHANNEL_ID)
                .setLargeIcon(icon)
                .setSmallIcon(R.drawable.ic_envelope)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setWhen(timeStamp)
                .setContentText(text)
                .setStyle(NotificationCompat.BigTextStyle().bigText(textBig))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .addAction(
                        R.drawable.ic_eye,
                        context.getString(R.string.mark_as_read),
                        getMarkAsReadIntent(messageId, peerId)
                )
                .setContentIntent(getOpenAppIntent(peerId, userName, photo))
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

        if (!unreadMessages[peerId].isNullOrEmpty()) {
            notificationManager.notify(peerId, notification)
        }
    }

    private fun createNotificationChannel(shouldRing: Boolean, shouldVibrate: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.app_name)
            val descriptionText = context.getString(R.string.app_name)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NOTIFICATIONS_CHANNEL_ID, name, importance)
            channel.description = descriptionText

            channel.setSound(if (shouldRing) RING_URI else null, null)

            channel.vibrationPattern = if (shouldVibrate) VIBRATE_PATTERN else null
            channel.enableVibration(shouldVibrate)

            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showForeground(service: Service) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.xvii_longpoll)
            val channel = NotificationChannel(SERVICE_CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
            val explainIntent = Intent(context, LongPollExplanationActivity::class.java)
            val explainPendingIntent = PendingIntent.getActivity(context, 0, explainIntent, 0)
            val notification = NotificationCompat.Builder(context, SERVICE_CHANNEL_ID)
                    .setContentIntent(explainPendingIntent)
                    .setShowWhen(false)
                    .setOngoing(true)
                    .setSmallIcon(R.drawable.shape_transparent)
                    .setContentTitle(context.getString(R.string.xvii_longpoll))
                    .setContentText(context.getString(R.string.longpoll_hint))
                    .build()
            service.startForeground(9999, notification)
        }
    }

    private fun getOpenAppIntent(peerId: Int, userName: String, photo: String?): PendingIntent {
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

    /**
     * returns title and photo by peerId
     * used for groups and users
     */
    private fun resolveSenderByPeerId(
            peerId: Int,
            onSuccess: (String, String?) -> Unit,
            onFail: () -> Unit
    ) {
        if (peerId < 0) {
            api.getGroups("${-peerId}")
                    .subscribeSmart({ groups ->
                        val group = groups.getOrElse(0) {
                            onFail()
                            return@subscribeSmart
                        }
                        onSuccess(group.name, group.photo100)
                    }, {
                        lw("resolve group error: $it")
                        onFail()
                    })
        } else {
            api.getUsers("$peerId")
                    .subscribeSmart({ users ->
                        val user = users.getOrElse(0) {
                            onFail()
                            return@subscribeSmart
                        }
                        onSuccess(user.fullName, user.photo100)
                    }, {
                        lw("resolving user error: $it")
                        onFail()
                    })
        }
    }

    @SuppressLint("CheckResult")
    fun getDialog(
            peerId: Int,
            onSuccess: (Dialog) -> Unit,
            onFail: () -> Unit) {
        appDb.dialogsDao().getDialogs(peerId)
                .compose(applySingleSchedulers())
                .subscribe(onSuccess) {
                    it.printStackTrace()
                    lw("loading from db error: ${it.message}")
                    onFail()
                }
    }

    private fun vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(VIBRATE_DELAY, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(VIBRATE_DELAY)
        }
    }

    private fun getConnectSingle(longPollServer: LongPollServer)
            = api.connectLongPoll("https://${longPollServer.server}", longPollServer.key, longPollServer.ts)

    @SuppressLint("CheckResult")
    private fun waitInBg(delayMs: Long) {
        Observable.timer(delayMs, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe {
                    isRunning = false
                }
    }

    private fun l(s: String) {
        Lg.i("[longpoll] $s")
    }

    private fun lw(s: String) {
        Lg.wtf("[longpoll] $s")
    }

    companion object {

        private const val NOTIFICATIONS_CHANNEL_ID = "xvii.notifications"
        private const val SERVICE_CHANNEL_ID = "xvii.service"

        private const val VIBRATE_DELAY = 60L
        private const val WAIT_DELAY = 1000L
        private const val NO_NETWORK_DELAY = 5000L

        private const val LAST_RUN_ALLOWED_DELAY = 45

        private val RING_URI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        private val VIBRATE_PATTERN = longArrayOf(0L, 200L)

        /**
         * watches for running
         */
        var lastRun = 0
            private set

        fun isRunning() = time() - lastRun < LAST_RUN_ALLOWED_DELAY
    }
}