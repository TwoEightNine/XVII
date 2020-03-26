package com.twoeightnine.root.xvii.activities

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.utils.DragTouchListener
import com.twoeightnine.root.xvii.utils.stylize
import kotlinx.android.synthetic.main.activity_content.*

/**
 * it is often needed to place the only fragment inside an activity
 * so this activity is for this case!
 * just extend it and override [createFragment]
 */
abstract class ContentActivity : BaseActivity() {

    protected open fun getLayoutId() = R.layout.activity_content

    abstract fun createFragment(intent: Intent?): Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())
        savedInstanceState ?: loadFragment(createFragment(intent))
        stylize()

        if (shouldEnableSwipeToBack()) {
            vDraggable.setOnTouchListener(DragTouchListener(this, flContainer, vShadow))
        }

        //android O fix bug orientation. https://stackoverflow.com/a/50832408
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.apply { loadFragment(createFragment(this)) }
    }

    override fun getThemeId() = R.style.AppTheme_Transparent

    protected fun loadFragment(fragment: Fragment) {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.flContainer, fragment)
                .commitAllowingStateLoss()
    }

    protected fun getFragment() = supportFragmentManager.findFragmentById(R.id.flContainer)

    protected fun shouldEnableSwipeToBack() = true

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.open_activity, R.anim.close_activity)
    }
}