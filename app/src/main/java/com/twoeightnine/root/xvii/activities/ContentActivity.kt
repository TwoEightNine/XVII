package com.twoeightnine.root.xvii.activities

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.utils.stylize

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
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.apply { loadFragment(createFragment(this)) }
    }

    protected fun loadFragment(fragment: Fragment) {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.flContainer, fragment)
                .commitAllowingStateLoss()
    }

    protected fun getFragment() = supportFragmentManager.findFragmentById(R.id.flContainer)
}