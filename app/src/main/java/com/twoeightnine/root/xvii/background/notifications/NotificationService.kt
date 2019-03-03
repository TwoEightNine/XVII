package com.twoeightnine.root.xvii.background.notifications

import android.app.Service
import android.content.Context
import android.content.Intent


class NotificationService : Service() {

    companion object {

        fun launch(context: Context, intent: Intent = Intent(context, NotificationService::class.java)) {
            context.startService(intent)
        }

        fun stop(context: Context, intent: Intent = Intent(context, NotificationService::class.java)) {
            context.stopService(intent)
        }
    }

    private val core by lazy { NotificationsCore(applicationContext) }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Thread {
            core.run()
        }.start()
        return START_REDELIVER_INTENT
    }

    override fun onCreate() {
        super.onCreate()
        core.onCreate()
    }

    override fun onDestroy() {
        core.onDestroy()
        super.onDestroy()
        launch(applicationContext)
    }

    override fun onBind(intent: Intent?) = null
}
