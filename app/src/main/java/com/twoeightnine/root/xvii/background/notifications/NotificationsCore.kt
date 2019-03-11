package com.twoeightnine.root.xvii.background.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.text.Html
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.BuildConfig
import com.twoeightnine.root.xvii.activities.RootActivity
import com.twoeightnine.root.xvii.dagger.ApiService
import com.twoeightnine.root.xvii.managers.Lg
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.managers.Session
import com.twoeightnine.root.xvii.model.LongPollEvent
import com.twoeightnine.root.xvii.model.LongPollServer
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.model.UserDb
import com.twoeightnine.root.xvii.model.response.LongPollHistoryResponse
import com.twoeightnine.root.xvii.model.response.LongPollResponse
import com.twoeightnine.root.xvii.utils.*
import io.reactivex.disposables.CompositeDisposable
import io.realm.Realm
import javax.inject.Inject
import android.app.NotificationChannel
import android.os.*
import com.twoeightnine.root.xvii.R


class NotificationsCore(private val context: Context) {

    @Inject
    lateinit var api: ApiService

    private var longPollServer: LongPollServer? = null
    private var lastCount: Int = 0
    private var allowVibrate: Boolean = false
    private var showNotif: Boolean = false
    private var showNotifChats: Boolean = false
    private var showName: Boolean = false
    private var sound: Boolean = false
    private var ledLights: Boolean = false
    private var showContent: Boolean = false
    private var muteList: MutableList<Int>? = null
    private var color: Int = Color.WHITE
    private var users = hashMapOf<Int, User>()

    private var token: String? = null
    private var isRunning = false

    private val handler = Handler()
    private val disposables = CompositeDisposable()

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

    fun run() {
        while (true) {
            if (!isRunning) {
                isRunning = true
                initPrefs()
                l("on ${longPollServer?.ts}")
                getUpdates()
            }
            Thread.sleep(WAIT_DELAY)
        }
    }

    fun onCreate() {
        App.appComponent?.inject(this)
        l("created")
    }

    fun onDestroy() {
        l("destroyed")
        restartService()
//        startNotificationService(context)
    }

    private fun initPrefs() {
        token = Session.token
        longPollServer = Session.longPoll
        allowVibrate = Prefs.vibrate
        showName = Prefs.showName
        showNotif = Prefs.showNotifs
        showNotifChats = Prefs.showNotifsChats
        sound = Prefs.sound
        ledLights = Prefs.ledLights
        if (Prefs.isNight) {
            color = Prefs.color
        }
        muteList = Prefs.muteList
        showContent = Prefs.showContent
        if (users.isEmpty()) {
            CacheHelper.getAllUsersAsync {
                users = it
            }
        }
        Session.serviceLastAction = time()
    }

    private fun getUpdates() {
        api.connect(
                "https://" + longPollServer!!.server!!,
                longPollServer!!.key ?: "",
                longPollServer!!.ts,
                "a_check", 25, 2
        )
                .compose(applySchedulers())
                .subscribe({ response ->
                    val resp = if (BuildConfig.DEBUG) response.toString() else response.toStringSafe()
                    if (response != null && response.failed == 0) {
                        Lg.i(resp)
                        sendResult(response)
                    } else {
                        Lg.i("updating reason: $resp")
                        updateLongPoll()
                    }
                }, { error ->
                    Lg.wtf("SERVICE got error: ${error.message}")
                    if (error.message?.startsWith("Unable") == true) { //no internet
                        handler.postDelayed({ restartService() }, NO_INTERNET_DELAY)
                    } else {
                        Lg.i("updating reason: ${error.message}")
                        updateLongPoll()
                    }
                }).let { disposables.add(it) }
    }

    private fun sendResult(response: LongPollResponse) {
        if (Session.token != token) {
            l("INVALID TOKEN")
            updateLongPoll()
            return
        }
        Session.timeStamp = response.ts
        val intentResult = Intent(NAME)
        val extras = Bundle()
        extras.putSerializable(RESULT, response)
        intentResult.putExtras(extras)
        androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(context).sendBroadcast(intentResult)
        checkForNotif(response)
        cacheResponse(response)
        restartService()
    }

    private fun cacheResponse(response: LongPollResponse) {
        if (response.updates == null) return
        for (upd in response.updates!!) {
            val event = LongPollEvent(upd)
            if (event.type != LongPollEvent.NEW_MESSAGE) {
                continue
            }

            val message = getMessageFromLongPollFull(event, users, false)
            CacheHelper.saveMessageAsync(message)
        }
    }

    private fun updateLongPoll() {
        api.getLongPollServer()
                .subscribeSmart({ response ->
                    checkHistory(response)
                }, {
                    handler.postDelayed({ restartService() }, NO_INTERNET_DELAY)
                }).let { disposables.add(it) }
    }

    private fun checkHistory(longPoll: LongPollServer) {
        val ts = longPoll.ts
        val oldTs = Session.timeStamp
        Lg.i("checking history $ts ${Session.timeStamp}")
        var events = ts - Session.timeStamp
        Session.longPoll = longPoll
        if (events == 0) {
            restartService()
            return
        }

        if (events < 1000) events = 1000
        api.getLongPollHistory(oldTs, events)
                .subscribeSmart({ response ->
                    sendHistory(response, ts)
                }, {
                    Lg.wtf("history error: $it")
                    restartService()
                }).let { disposables.add(it) }
    }

    private fun sendHistory(historyResponse: LongPollHistoryResponse, newTs: Int) {
        Lg.i(if (BuildConfig.DEBUG) historyResponse.toString() else historyResponse.toStringSafe())
        CacheHelper.saveMessagesAsync(historyResponse.messages?.items ?: mutableListOf())
        val rawUpds = mutableListOf<MutableList<Any>>()
        historyResponse.messages?.items?.forEach {
            rawUpds.add(LongPollEvent(it).getRawUpd())
        }
        val resp = LongPollResponse(rawUpds, newTs)
        sendResult(resp)
    }

    private fun restartService() {
        isRunning = false
        l("restarting")
    }

    private fun checkForNotif(response: LongPollResponse) {
        for (item in response.updates!!) {
            val event = LongPollEvent(item)
            if (event.type == LongPollEvent.NEW_COUNT) {
                val decrease = lastCount > event.count
                lastCount = event.count
                if (decrease) {
                    notificationManager.cancelAll()
                }
            }

            if (event.type == LongPollEvent.NEW_MESSAGE &&
                    event.out == 0 &&
                    showNotif) {

                if (muteList?.contains(event.userId) == true) return

                if (showNotifChats || event.userId < 2000000000) {

                    if (!isInForeground()) {
                        if (allowVibrate) {
                            vibrate()
                        }
                        if (sound) {
                            ringtone.play()
                        }
                    }
                }

                val content = if (showContent) {
                    event.message
                } else {
                    context.getString(R.string.content_hidden)
                }

                try {
                    val realm = Realm.getDefaultInstance()
                    val realmData = realm
                            .where(UserDb::class.java)
                            .equalTo("id", event.userId)
                    val realmUser = realmData.findFirst()
                    val userName = if (realmUser != null && event.userId in 0..2000000000) {
                        User(realmUser).fullName()
                    } else {
                        event.title
                    }
                    if (realmUser != null && showName && event.userId in 0..2000000000) {
                        loadBitmapIcon(User(realmUser).photoMax) {
                            showNotification(content, event.userId, userName, userName, it)
                        }

                    } else {
                        showNotification(content, event.userId, userName)
                    }
                } catch (e: Exception) {
                    showNotification(content, event.userId, event.message)
                }
            }
        }
    }

    private fun showNotification(
            content: String,
            peerId: Int,
            userName: String,
            title: String = context.getString(R.string.app_name),
            icon: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.xvii128)
    ) {

        val intent = Intent(context, RootActivity::class.java)
        intent.putExtra(RootActivity.USER_ID, peerId)
        intent.putExtra(RootActivity.TITLE, userName)
        val pIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.app_name)
            val descriptionText = context.getString(R.string.app_name)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = descriptionText
            channel.setSound(null, null)

            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setLargeIcon(icon)
                .setSmallIcon(com.twoeightnine.root.xvii.R.drawable.ic_message)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setContentText(Html.fromHtml(content))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setContentIntent(pIntent)

        if (ledLights) {
            builder.setLights(color, 500, 500)
        }

        notificationManager.notify(peerId, builder.build())
    }

    private fun vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(VIBRATE_DELAY, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(VIBRATE_DELAY)
        }
    }

    private fun l(s: String) {
        Lg.i("[service] $s")
    }

    companion object {
        var NAME = "huyhuyhuy"
        var RESULT = "Result"

        private const val CHANNEL_ID = "xvii.notifications"

        private const val VIBRATE_DELAY = 60L
        private const val WAIT_DELAY = 1000L
        private const val NO_INTERNET_DELAY = 5000L
    }
}
