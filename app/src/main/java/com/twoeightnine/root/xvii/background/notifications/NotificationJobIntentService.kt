package com.twoeightnine.root.xvii.background.notifications

import android.content.Context
import android.content.Intent
import android.support.v4.app.JobIntentService


class NotificationJobIntentService : JobIntentService() {

    companion object {
        private const val JOB_ID = 113

        fun enqueue(context: Context, intent: Intent = Intent(context, NotificationJobIntentService::class.java)) {
            enqueueWork(context, NotificationJobIntentService::class.java, JOB_ID, intent)
        }
    }

    private val core by lazy { NotificationsCore(applicationContext) }

    override fun onHandleWork(intent: Intent) {
        core.run()
    }

    override fun onCreate() {
        super.onCreate()
        core.onCreate()
    }

    override fun onDestroy() {
        core.onDestroy()
        super.onDestroy()
        enqueue(applicationContext)
    }
}
