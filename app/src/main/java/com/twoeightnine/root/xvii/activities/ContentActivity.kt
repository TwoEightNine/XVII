package com.twoeightnine.root.xvii.activities

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.managers.Style

abstract class ContentActivity : BaseActivity() {

    abstract fun getLayoutId(): Int

    abstract fun getFragment(args: Bundle?): Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())
        savedInstanceState ?: loadFragment(getFragment(intent.extras))
        Style.setStatusBar(this)
    }

    protected fun loadFragment(fragment: Fragment) {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.flContainer, fragment)
                .commitAllowingStateLoss()
    }
}