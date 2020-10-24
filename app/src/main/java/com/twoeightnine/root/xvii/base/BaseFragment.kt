package com.twoeightnine.root.xvii.base

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.utils.stylize
import kotlinx.android.synthetic.main.fragment_ui_kit.*
import kotlinx.android.synthetic.main.toolbar.*

abstract class BaseFragment : Fragment() {

    @LayoutRes
    abstract fun getLayoutId(): Int

    protected val baseActivity
        get() = activity as? BaseActivity

    protected val contextOrThrow: Context
        get() = context ?: throw IllegalStateException("Context has leaked away!")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View = View.inflate(activity, getLayoutId(), null)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.requestApplyInsets(view)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initToolbar()
    }

    protected fun initToolbar() {
        if (toolbar != null) {
            if (xviiToolbar != null) {
                baseActivity?.also(xviiToolbar::setupWith)
                return
            }
            baseActivity?.setSupportActionBar(toolbar)
            val actionBar = baseActivity?.supportActionBar
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true)
                actionBar.setHomeAsUpIndicator(R.drawable.ic_back)
                actionBar.setHomeButtonEnabled(true)
                actionBar.setDisplayUseLogoEnabled(false)
                context?.let {
                    toolbar?.setTitleTextColor(ContextCompat.getColor(it, R.color.toolbar_text))
                    toolbar?.setSubtitleTextColor(ContextCompat.getColor(it, R.color.toolbar_subtext))
                }
            }
            toolbar.stylize()
            val am = context?.resources?.assets
            for (i in 0 until toolbar.childCount) {
                val view = toolbar.getChildAt(i)
                if (view is TextView) {
                    view.typeface = Typeface.createFromAsset(am, "fonts/medium.ttf")
                }
            }
            ViewCompat.setOnApplyWindowInsetsListener(toolbar) { view, insets ->
                view.setPadding(0, insets.systemWindowInsetTop, 0, 0)
                view.layoutParams.apply {
                    val toolbarHeight = context?.resources?.getDimensionPixelSize(R.dimen.toolbar_height)
                            ?: 0
                    height = toolbarHeight + insets.systemWindowInsetTop
                    view.layoutParams = this
                }
                insets
            }
        }
    }

    fun setTitle(title: CharSequence) {
        toolbar?.title = title
        val actionBar = baseActivity?.supportActionBar
        if (actionBar != null) {
            actionBar.title = title
        }
    }

    fun setSubtitle(subtitle: CharSequence) {
        baseActivity?.supportActionBar?.subtitle = subtitle
    }

    fun updateTitle(title: String = "", subtitle: String = "") {
        baseActivity?.supportActionBar?.title = title
        baseActivity?.supportActionBar?.subtitle = subtitle
    }
}