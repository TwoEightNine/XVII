package com.twoeightnine.root.xvii.activities

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.utils.ExceptionHandler
import com.twoeightnine.root.xvii.utils.NightModeHelper
import com.twoeightnine.root.xvii.utils.stylize
import com.twoeightnine.root.xvii.utils.stylizeAll
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper

/**
 * all its children will support theme applying
 */
abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler(this))

        window.decorView.systemUiVisibility =
                // Tells the system that the window wishes the content to
                // be laid out at the most extreme scenario. See the docs for
                // more information on the specifics
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        // Tells the system that the window wishes the content to
                        // be laid out as if the navigation bar was hidden
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    override fun setContentView(@LayoutRes layoutResID: Int) {
        updateConfig()
        super.setContentView(layoutResID)
    }

    override fun onResume() {
        updateConfig()
        super.onResume()
    }

    override fun onOptionsItemSelected(item: MenuItem?) = when (item?.itemId) {
        android.R.id.home -> {
            onBackPressed()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    protected open fun getThemeId() = R.style.AppTheme

    protected open fun styleScreen(container: ViewGroup) {
        container.stylizeAll()
        stylize()
    }

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

}