package com.twoeightnine.root.xvii.base

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.annotation.LayoutRes
import androidx.annotation.MenuRes
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import com.twoeightnine.root.xvii.uikit.Munch
import com.twoeightnine.root.xvii.uikit.paint
import kotlinx.android.synthetic.main.fragment_ui_kit.*

abstract class BaseFragment : Fragment() {

    @LayoutRes
    abstract fun getLayoutId(): Int

    protected val baseActivity
        get() = activity as? BaseActivity

    @Deprecated("use requireContext() if needed", replaceWith = ReplaceWith("requireContext()"))
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
        xviiToolbar?.apply {
            baseActivity?.also(::setupWith)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        val menuId = getMenu()
        if (menuId != 0) {
            inflater.inflate(menuId, menu)
            menu.paint(Munch.color.color)
        }
    }

    @MenuRes
    protected open fun getMenu(): Int = 0

    protected fun setStatusBarLight(isLight: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            baseActivity?.window?.decorView?.apply {
                systemUiVisibility = if (isLight) {
                    systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                } else {
                    systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                }
            }
        }
    }
}