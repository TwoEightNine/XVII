package com.twoeightnine.root.xvii.activities

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
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
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    override fun setContentView(@LayoutRes layoutResID: Int) {
        NightModeHelper.updateConfig(
                if (Prefs.isLightTheme) {
                    Configuration.UI_MODE_NIGHT_NO
                } else {
                    Configuration.UI_MODE_NIGHT_YES
                },
                this, getThemeId()
        )
        super.setContentView(layoutResID)
    }

    override fun onResume() {
        NightModeHelper.updateConfig(
                if (Prefs.isLightTheme) {
                    Configuration.UI_MODE_NIGHT_NO
                } else {
                    Configuration.UI_MODE_NIGHT_YES
                },
                this, getThemeId()
        )
        super.onResume()
    }

    override fun onOptionsItemSelected(item: MenuItem?) = when(item?.itemId) {
        android.R.id.home -> {
            onBackPressed()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    protected open fun getThemeId() = R.style.AppTheme

    protected fun styleScreen(container: ViewGroup) {
        container.stylizeAll()
        stylize()
    }

}