package com.twoeightnine.root.xvii.fragments

import android.content.Context
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.annotation.Nullable
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.view.*
import butterknife.BindView
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.activities.BaseActivity
import com.twoeightnine.root.xvii.activities.RootActivity
import com.twoeightnine.root.xvii.managers.Style

open class BaseFragment: Fragment() {

    @Nullable
    @BindView(R.id.toolbar) @JvmField
    var toolbar: Toolbar? = null

    private var isNew = false
    private var isSearchOpen = false

    protected lateinit var rootActivity: RootActivity

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
        rootActivity = activity as RootActivity
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
            rootActivity.setSupportActionBar(toolbar)
            val actionBar = rootActivity.supportActionBar
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true)
                actionBar.setHomeAsUpIndicator(R.drawable.ic_menu)
                actionBar.setHomeButtonEnabled(true)
                actionBar.setDisplayUseLogoEnabled(false)
                toolbar!!.setTitleTextColor(ContextCompat.getColor(rootActivity, R.color.toolbar_text))
                toolbar!!.setSubtitleTextColor(ContextCompat.getColor(rootActivity, R.color.toolbar_subtext))
            }

        }
    }

    fun setTitle(title: CharSequence) {
        toolbar?.title = title
        val actionBar = rootActivity.supportActionBar
        if (actionBar != null) {
            actionBar.title = title
        }
    }

    fun setSubtitle(subtitle: CharSequence) {
        if (rootActivity.supportActionBar != null)
            rootActivity.supportActionBar!!.subtitle = subtitle
    }

    fun updateTitle(title: String = "", subtitle: String = "") {
        rootActivity.supportActionBar?.title = title
        rootActivity.supportActionBar?.subtitle = subtitle
    }

    open fun onBackPressed() = false

    protected fun styleScreen(container: ViewGroup? = null) {
        if (toolbar != null) Style.forToolbar(toolbar!!)
        Style.forAll(container)
        Style.setStatusBar(rootActivity)
    }
}