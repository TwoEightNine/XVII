package com.twoeightnine.root.xvii.background.messaging

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.lg.L
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.utils.applySchedulers
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MessageDestructionService : Service() {

    @Inject
    lateinit var api: ApiService

    private var timerDisposable: Disposable? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        App.appComponent?.inject(this)
        val timeToLive = intent?.extras?.getInt(ARG_TIME_TO_LIVE) ?: 0
        val messageId = intent?.extras?.getInt(ARG_MESSAGE_ID) ?: 0

        timerDisposable = Observable.interval(1L, TimeUnit.SECONDS)
                .map { timeToLive - it }
                .map { it.toInt() }
                .subscribe { remain ->
                    updateNotification(messageId, remain)
                    if (remain == 0) {
                        doDestruction(messageId)
                    }
                }
        l("timer started for $timeToLive seconds")

        createChannel()
        updateNotification(messageId, timeToLive)

        return START_STICKY
    }

    override fun onDestroy() {
        timerDisposable?.dispose()
        stopForeground(true)
        l("service destroyed!")
        super.onDestroy()
    }

    @SuppressLint("CheckResult")
    private fun doDestruction(messageId: Int) {
        timerDisposable?.dispose()
        api.deleteMessages("$messageId", 1)
                .compose(applySchedulers())
                .subscribe {
                    l("message $messageId has been deleted")
                    stopSelf()
                }
    }

    private fun updateNotification(messageId: Int, remain: Int) {
        val title = if (remain == 0) {
            getString(R.string.destructor_process_title)
        } else {
            getString(R.string.destructor_title, remain)
        }
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(getString(R.string.destructor_hint))
                .setSmallIcon(R.drawable.ic_clock)
                .build()
        startForeground(messageId.hashCode(), notification)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val name = getString(R.string.destructor_name)
            val channel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW)
                    .apply {
                        enableVibration(false)
                        setSound(null, null)
                    }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun l(s: String) {
        L.tag(TAG).log(s)
    }

    companion object {

        private const val TAG = "message destructor"

        const val CHANNEL_ID = "xvii.message_destructor"

        const val ARG_MESSAGE_ID = "messageId"
        const val ARG_TIME_TO_LIVE = "timeToLive"

        fun start(context: Context?, messageId: Int, timeToLive: Int) {
            context?.startService(Intent(context, MessageDestructionService::class.java).apply {
                putExtra(ARG_MESSAGE_ID, messageId)
                putExtra(ARG_TIME_TO_LIVE, timeToLive)
            })
        }
    }
}