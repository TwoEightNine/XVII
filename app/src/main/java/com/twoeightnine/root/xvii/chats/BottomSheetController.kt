package com.twoeightnine.root.xvii.chats

import android.support.design.widget.BottomSheetBehavior
import android.view.View

/**
 * Created by msnthrp on 17/01/18.
 */
class BottomSheetController<V: View>(private val sheet: V) {

    private val behavior = BottomSheetBehavior.from(sheet)

    fun open() {
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    fun close() {
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    fun isOpen() = behavior.state == BottomSheetBehavior.STATE_EXPANDED

}