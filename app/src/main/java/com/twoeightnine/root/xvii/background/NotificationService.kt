package com.twoeightnine.root.xvii.background

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Bundle
import android.os.Handler
import android.os.Vibrator
import android.support.v4.app.NotificationCompat
import android.support.v4.content.LocalBroadcastManager
import android.text.Html
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.BuildConfig
import com.twoeightnine.root.xvii.R
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
import io.realm.Realm
import javax.inject.Inject


class NotificationService : Service() {

    @Inject
    lateinit var api: ApiService

    private var longPollServer: LongPollServer? = null
    private var lastCount: Int = 0
    private var allowVibrate: Boolean = false
    private var showNotif: Boolean = false
    private var showNotifChats: Boolean = false
    private var showName: Boolean = false
    private var sound: Boolean = false
    private var showContent: Boolean = false
    private var muteList: MutableList<Int>? = null
    private var users = hashMapOf<Int, User>()

    private var token: String? = null
    private var isRunning = false

    private val handler = Handler()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isRunning) {
            isRunning = true
            Thread {
                initPrefs()
                l("on ${longPollServer?.ts}")
                getUpdates()
            }.start()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent) = null

    override fun onCreate() {
        super.onCreate()
        App.appComponent?.inject(this)
        l("created")
    }

    override fun onDestroy() {
        l("destroyed")
        restartService()
    }

    private fun initPrefs() {
        token = Session.token
        longPollServer = Session.longPoll
        allowVibrate = Prefs.vibrate
        showName = Prefs.showName
        showNotif = Prefs.showNotifs
        showNotifChats = Prefs.showNotifsChats
        sound = Prefs.sound
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
                    if (error.message?.startsWith("Unable") ?: false) { //no internet
                        handler.postDelayed({ restartService() }, NO_INTERNET_DELAY)
                    } else {
                        Lg.i("updating reason: ${error.message}")
                        updateLongPoll()
                    }
                })
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
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intentResult)
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
                })
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
                })
    }

    private fun sendHistory(historyResponse: LongPollHistoryResponse, newTs: Int) {
        Lg.i(historyResponse.toStringSafe())
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
        sendBroadcast(Intent(RestarterBroadcastReceiver.RESTART_ACTION))
    }

    private fun checkForNotif(response: LongPollResponse) {
        for (item in response.updates!!) {
            val event = LongPollEvent(item)
            if (event.type == LongPollEvent.NEW_COUNT) {
                val decrease = lastCount > event.count
                lastCount = event.count
                if (decrease) closeNotification()
            }

            if (event.type == LongPollEvent.NEW_MESSAGE &&
                    event.out == 0 &&
                    showNotif) {

                if (muteList?.contains(event.userId) ?: false) return

                if (showNotifChats || event.userId < 2000000000) {

                    if (allowVibrate) {
                        vibrate()
                    }
                    if (sound) {
                        playRingtone()
                    }
                }

                val content = if (showContent) {
                    event.message
                } else {
                    getString(R.string.content_hidden)
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
                        loadBitmapIcon(User(realmUser).photoMax, {
                            showNotification(content, event.userId, userName, userName, it)
                        })

                    } else {
                        showNotification(content, event.userId, userName)
                    }
                } catch (e: Exception) {
                    showNotification(content, event.userId, event.message)
                }
            }
        }
    }

    private fun showNotification(content: String,
                                 peerId: Int,
                                 userName: String,
                                 title: String = getString(R.string.app_name),
                                 icon: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.xvii128)) {
        val intent = Intent(this, RootActivity::class.java)
        intent.putExtra(RootActivity.USER_ID, peerId)
        intent.putExtra(RootActivity.TITLE, userName)
        val pIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        val mBuilder = NotificationCompat.Builder(this)
                .setLargeIcon(icon)
                .setSmallIcon(R.drawable.ic_message)
                .setContentTitle(title)
                .setContentText(Html.fromHtml(content))
                .setContentIntent(pIntent)

        val mNotifyMgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotifyMgr.notify(NOTIFICATION, mBuilder.build())
    }

    private fun closeNotification() {
        val mNotifyMgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotifyMgr.cancel(NOTIFICATION)
    }

    private fun vibrate() {
        val vi = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vi.vibrate(VIBRATE_DELAY)
    }

    private fun playRingtone() {
        RingtoneManager.getRingtone(
                applicationContext,
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        ).play()
    }

    private fun l(s: String) {
        Lg.i("[service] $s")
    }

    companion object {
        var NOTIFICATION = 1337
        var NAME = "huyhuyhuy"
        var RESULT = "Result"

        private val VIBRATE_DELAY = 200L
        private val NO_INTERNET_DELAY = 5000L
    }
}
