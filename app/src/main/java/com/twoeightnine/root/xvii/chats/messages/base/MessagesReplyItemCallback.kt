package com.twoeightnine.root.xvii.chats.messages.base

import android.graphics.Canvas
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.utils.VibrationHelper

class MessagesReplyItemCallback(
        private val onReplySwiped: (Int) -> Unit
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    private var invoked = false

    override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
    ) = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

    override fun getSwipeEscapeVelocity(defaultValue: Float) = UNREACHABLE_VALUE

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder) = UNREACHABLE_VALUE

    override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX / SWIPE_LIMIT_FACTOR, dY, actionState, isCurrentlyActive)
        when {
            !invoked && dX < INVOKE_THRESHOLD_PX -> {
                invoked = true
                VibrationHelper.vibrateHaptic()
                onReplySwiped(viewHolder.adapterPosition)
            }
            dX == .0f -> {
                invoked = false
            }
        }
    }

    companion object {
        private const val UNREACHABLE_VALUE = 100000f
        private const val SWIPE_LIMIT_FACTOR = 2
        private const val INVOKE_THRESHOLD_PX = -250f
    }
}