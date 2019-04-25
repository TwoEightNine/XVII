package com.twoeightnine.root.xvii.utils

import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.twoeightnine.root.xvii.views.UserLockBottomSheetBehavior

/**
 * Created by msnthrp on 17/01/18.
 */
class BottomSheetController<V: View>(private val sheet: V,
                                     private val hide: View? = null,
                                     private val onClosed: () -> Unit = {}) {

    private val behavior = UserLockBottomSheetBehavior.from(sheet)

    init {
        hide?.setOnClickListener { close() }
    }

    fun open() {
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    fun close() {
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        onClosed.invoke()
    }

    fun isOpen() = behavior.state == BottomSheetBehavior.STATE_EXPANDED

}