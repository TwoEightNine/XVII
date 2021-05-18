package com.twoeightnine.root.xvii.base

import android.os.Bundle
import android.view.*
import androidx.annotation.LayoutRes
import androidx.annotation.MenuRes
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.twoeightnine.root.xvii.lg.L
import com.twoeightnine.root.xvii.uikit.Munch
import com.twoeightnine.root.xvii.uikit.paint
import kotlinx.android.synthetic.main.fragment_ui_kit.*

abstract class BaseFragment : Fragment() {

    @LayoutRes
    abstract fun getLayoutId(): Int

    private val baseActivity
        get() = activity as? BaseActivity

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
        l("onViewCreated")
        ViewCompat.requestApplyInsets(view)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        l("onActivityCreated")
        xviiToolbar?.apply {
            baseActivity?.also(::setupWith)
        }
    }

    override fun onResume() {
        super.onResume()
        l("onResume")
    }

    override fun onPause() {
        l("onPause")
        super.onPause()
    }

    override fun onDestroyView() {
        l("onDestroyView")
        super.onDestroyView()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        val menuId = getMenu()
        if (menuId != 0) {
            inflater.inflate(menuId, menu)
            menu.paint(Munch.color.color)
        }
    }

    protected fun onBackPressed() {
        baseActivity?.onBackPressed()
    }

    @MenuRes
    protected open fun getMenu(): Int = 0

    protected fun setStatusBarLight(isLight: Boolean) {
        baseActivity?.window?.decorView?.apply {
            systemUiVisibility = if (isLight) {
                systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
        }
    }

    protected fun <T> LiveData<T>.observe(observer: (T) -> Unit) {
        observe(viewLifecycleOwner, Observer(observer))
    }

    private fun l(event: String) {
        L.tag("lifecycle").log("${javaClass.simpleName} $event")
    }
}