package com.twoeightnine.root.xvii.utils

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import com.twoeightnine.root.xvii.activities.LoginActivity
import com.twoeightnine.root.xvii.activities.PinActivity
import com.twoeightnine.root.xvii.managers.Session

class AppLifecycleTracker : Application.ActivityLifecycleCallbacks {

    private var numStarted = 0

    private fun onForeground(context: Context) {
        if (Session.needToPromptPin()) {
            PinActivity.launch(context, PinActivity.ACTION_ENTER)
        }
    }

    private fun onBackground() {

    }

    private fun ignore(activity: Activity?) =
            activity is LoginActivity ||
                    activity is PinActivity

    override fun onActivityStarted(activity: Activity?) {
        if (activity == null || ignore(activity)) return

        if (numStarted == 0) onForeground(activity)
        numStarted++
    }

    override fun onActivityStopped(activity: Activity?) {
        if (ignore(activity)) return

        numStarted--
        if (numStarted == 0) onBackground()
    }

    override fun onActivityPaused(activity: Activity?) {}

    override fun onActivityResumed(activity: Activity?) {}

    override fun onActivityDestroyed(activity: Activity?) {}

    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {}

    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {}
}