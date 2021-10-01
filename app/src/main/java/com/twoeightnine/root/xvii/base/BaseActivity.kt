/*
 * xvii - messenger for vk
 * Copyright (C) 2021  TwoEightNine
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.twoeightnine.root.xvii.base

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.background.longpoll.core.LongPollCore
import com.twoeightnine.root.xvii.lg.L
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.utils.ExceptionHandler
import com.twoeightnine.root.xvii.utils.getTime
import com.twoeightnine.root.xvii.utils.isAndroid10OrHigher
import com.twoeightnine.root.xvii.utils.startNotificationService
import global.msnthrp.xvii.uikit.utils.DisplayUtils
import io.github.inflationx.viewpump.ViewPumpContextWrapper

/**
 * all its children will support theme applying
 */
abstract class BaseActivity : AppCompatActivity() {

    private val almostTransparent by lazy {
        ContextCompat.getColor(this, R.color.almost_transparent)
    }
    private val navBarColor by lazy {
        ContextCompat.getColor(this, R.color.navigation_bar)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler(this))
        DisplayUtils.initIfNot(this)

        window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        if (Prefs.isLightTheme) {
            window.decorView.systemUiVisibility =
                    window.decorView.systemUiVisibility or
                            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Prefs.isLightTheme) {
            window.decorView.systemUiVisibility =
                    window.decorView.systemUiVisibility or
                            View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    override fun setContentView(@LayoutRes layoutResID: Int) {
        updateConfig()
        super.setContentView(layoutResID)
    }

    override fun onNewIntent(intent: Intent?) {
        updateConfig()
        super.onNewIntent(intent)
    }

    override fun onResume() {
        updateConfig()
        super.onResume()
        runServiceIfDown()
        window.statusBarColor = getStatusBarColor()
        window.navigationBarColor = if (isAndroid10OrHigher()) {
            almostTransparent
        } else {
            getNavigationBarColor()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            onBackPressed()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    protected open fun getThemeId() = R.style.AppTheme

    protected open fun getStatusBarColor() = Color.TRANSPARENT

    protected open fun getNavigationBarColor() = navBarColor

    protected open fun shouldRunService() = true

    private fun updateConfig() {
        AppCompatDelegate.setDefaultNightMode(when {
            Prefs.isLightTheme -> AppCompatDelegate.MODE_NIGHT_NO
            else -> AppCompatDelegate.MODE_NIGHT_YES
        })
    }

    private fun runServiceIfDown() {
        if (shouldRunService() && !LongPollCore.isProbablyRunning()) {
            L.tag("longpoll")
                    .log("inactive since ${getTime(LongPollCore.lastRun, withSeconds = true)}")
            Handler(Looper.getMainLooper())
                    .postDelayed({ startNotificationService(this) }, 500L)
        }
    }

}