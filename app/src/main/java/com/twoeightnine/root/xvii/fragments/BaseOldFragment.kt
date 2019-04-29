package com.twoeightnine.root.xvii.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.activities.BaseActivity
import com.twoeightnine.root.xvii.utils.stylize
import com.twoeightnine.root.xvii.utils.stylizeAll
import kotlinx.android.synthetic.main.toolbar.*

open class BaseOldFragment: Fragment() {

    private var isNew = false

    protected val rootActivity: BaseActivity?
        get() = activity as? BaseActivity

    protected val safeActivity: FragmentActivity
        get() = activity ?: throw Exception()

    protected val safeContext: Context
        get() = context ?: throw Exception()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        isNew = true
    }

    override fun onStart() {
        super.onStart()
        isNew = false
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?) = inflater.inflate(getLayout(), null)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        if (isNew) {
            onNew(view)
        } else {
            onRecovered(view)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initToolbar()
        styleScreen()
    }

    open fun bindViews(view: View) {}

    open fun onNew(view: View) {}

    open fun onRecovered(view: View) {}

    @LayoutRes
    open fun getLayout() = R.layout.activity_root

    protected fun initToolbar() {
        if (toolbar != null) {
            rootActivity?.setSupportActionBar(toolbar)
            val actionBar = rootActivity?.supportActionBar
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true)
                actionBar.setHomeAsUpIndicator(R.drawable.ic_back)
                actionBar.setHomeButtonEnabled(true)
                actionBar.setDisplayUseLogoEnabled(false)
                rootActivity?.let {
                    toolbar?.setTitleTextColor(ContextCompat.getColor(it, R.color.toolbar_text))
                    toolbar?.setSubtitleTextColor(ContextCompat.getColor(it, R.color.toolbar_subtext))
                }
            }

        }
    }

    fun setTitle(title: CharSequence) {
        toolbar?.title = title
        val actionBar = rootActivity?.supportActionBar
        if (actionBar != null) {
            actionBar.title = title
        }
    }

    fun setSubtitle(subtitle: CharSequence) {
        if (rootActivity?.supportActionBar != null)
            rootActivity?.supportActionBar?.subtitle = subtitle
    }

    fun updateTitle(title: String = "", subtitle: String = "") {
        rootActivity?.supportActionBar?.title = title
        rootActivity?.supportActionBar?.subtitle = subtitle
    }

    open fun onBackPressed() = false

    protected fun styleScreen(container: ViewGroup? = null) {
        toolbar?.stylize()
        container?.stylizeAll()
        rootActivity?.stylize()
    }
}