package com.twoeightnine.root.xvii.base

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.background.longpoll.core.LongPollCore
import com.twoeightnine.root.xvii.lg.L
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.utils.*
import io.github.inflationx.viewpump.ViewPumpContextWrapper

/**
 * all its children will support theme applying
 */
abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler(this))

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
            Color.TRANSPARENT
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

    protected open fun getNavigationBarColor() = ContextCompat.getColor(this, R.color.navigation_bar)

    private fun updateConfig() {
        NightModeHelper.updateConfig(
                if (Prefs.isLightTheme) {
                    Configuration.UI_MODE_NIGHT_NO
                } else {
                    Configuration.UI_MODE_NIGHT_YES
                },
                this, getThemeId()
        )
    }

    private fun runServiceIfDown() {
        if (!LongPollCore.isProbablyRunning()) {
            L.tag("longpoll")
                    .log("inactive since ${getTime(LongPollCore.lastRun, withSeconds = true)}")
            Handler(Looper.getMainLooper())
                    .postDelayed({ startNotificationService(this) }, 500L)
        }
    }

}