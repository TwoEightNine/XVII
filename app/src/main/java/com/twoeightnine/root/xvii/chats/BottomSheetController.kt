package com.twoeightnine.root.xvii.chats

import android.support.design.widget.BottomSheetBehavior
import android.view.View
import com.twoeightnine.root.xvii.views.UserLockBottomSheetBehavior

/**
 * Created by msnthrp on 17/01/18.
 */
class BottomSheetController<V: View>(private val sheet: V,
                                     private val hide: View? = null) {

    private val behavior = UserLockBottomSheetBehavior.from(sheet)

    init {
        hide?.setOnClickListener { close() }
    }

    fun open() {
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    fun close() {
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    fun isOpen() = behavior.state == BottomSheetBehavior.STATE_EXPANDED

}