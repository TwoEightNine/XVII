package com.twoeightnine.root.xvii.background.longpoll.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import com.twoeightnine.root.xvii.background.longpoll.LongPollCore


class NotificationService : Service() {

    companion object {

        fun launch(context: Context, intent: Intent = Intent(context, NotificationService::class.java)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context?, intent: Intent = Intent(context, NotificationService::class.java)) {
            context?.stopService(intent)
        }
    }

    private var core: LongPollCore? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val core = LongPollCore(applicationContext)
        Thread {
            core.run(intent)
        }.start()
        core.showForeground(this)
        this.core = core
        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        core?.onDestroy()
        super.onDestroy()
        launch(applicationContext)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
