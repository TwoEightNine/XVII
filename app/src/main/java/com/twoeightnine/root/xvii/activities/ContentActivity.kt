package com.twoeightnine.root.xvii.activities

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.utils.stylize

abstract class ContentActivity : BaseActivity() {

    protected open fun getLayoutId() = R.layout.activity_content

    abstract fun getFragment(args: Bundle?): Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())
        savedInstanceState ?: loadFragment(getFragment(intent.extras))
        stylize()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.apply { loadFragment(getFragment(extras)) }
    }

    protected fun loadFragment(fragment: Fragment) {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.flContainer, fragment)
                .commitAllowingStateLoss()
    }

    protected fun getFragment() = supportFragmentManager.findFragmentById(R.id.flContainer)
}