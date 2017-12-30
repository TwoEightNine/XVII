package com.twoeightnine.root.xvii.utils

import android.app.Activity
import android.content.res.Configuration

object NightModeHelper {

    fun updateConfig(uiNightMode: Int, activity: Activity?, theme: Int) {
        if (activity == null) {
            throw IllegalStateException("Activity went away?")
        }
        val newConfig = Configuration(activity.resources.configuration)
        newConfig.uiMode = newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()
        newConfig.uiMode = newConfig.uiMode or uiNightMode
        activity.resources.updateConfiguration(newConfig, null)
        activity.setTheme(theme)
    }
}