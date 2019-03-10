package com.twoeightnine.root.xvii.activities

import android.content.res.Configuration
import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import android.view.ViewGroup
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.utils.ExceptionHandler
import com.twoeightnine.root.xvii.utils.NightModeHelper

/**
 * all its children will support theme applying
 */
abstract class BaseActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler(this))
    }

    override fun setContentView(@LayoutRes layoutResID: Int) {
        NightModeHelper.updateConfig(
                if (Prefs.isNight) {
                    Configuration.UI_MODE_NIGHT_YES
                } else {
                    Configuration.UI_MODE_NIGHT_NO
                },
                this, R.style.AppTheme
        )
        super.setContentView(layoutResID)
    }

    override fun onResume() {
        super.onResume()
        NightModeHelper.updateConfig(
                if (Prefs.isNight) {
                    Configuration.UI_MODE_NIGHT_YES
                } else {
                    Configuration.UI_MODE_NIGHT_NO
                },
                this, R.style.AppTheme
        )
    }

    protected fun styleScreen(container: ViewGroup) {
        Style.forAll(container)
        Style.setStatusBar(this)
    }

}