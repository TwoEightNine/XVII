package com.twoeightnine.root.xvii.views

import android.content.Context
import androidx.coordinatorlayout.widget.CoordinatorLayout
import android.view.MotionEvent
import com.google.android.material.bottomsheet.BottomSheetBehavior
import android.util.AttributeSet
import android.view.View


/**
 * Created by twoeightnine on 1/18/18.
 */
class UserLockBottomSheetBehavior<V : View> : BottomSheetBehavior<V> {

    constructor() : super()

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onInterceptTouchEvent(parent: androidx.coordinatorlayout.widget.CoordinatorLayout,
                                       child: V,
                                       event: MotionEvent) = false

    override fun onTouchEvent(parent: androidx.coordinatorlayout.widget.CoordinatorLayout,
                              child: V,
                              event: MotionEvent) = false

    override fun onStartNestedScroll(coordinatorLayout: androidx.coordinatorlayout.widget.CoordinatorLayout,
                                     child: V,
                                     directTargetChild: View,
                                     target: View,
                                     nestedScrollAxes: Int) = false

    override fun onNestedPreScroll(coordinatorLayout: androidx.coordinatorlayout.widget.CoordinatorLayout,
                                   child: V,
                                   target: View, dx: Int, dy: Int,
                                   consumed: IntArray) {}

    override fun onStopNestedScroll(coordinatorLayout: androidx.coordinatorlayout.widget.CoordinatorLayout,
                                    child: V,
                                    target: View) {}

    override fun onNestedPreFling(coordinatorLayout: androidx.coordinatorlayout.widget.CoordinatorLayout,
                                  child: V, target: View,
                                  velocityX: Float, velocityY: Float) = false

    companion object {

        /**
         * A utility function to get the [BottomSheetBehavior] associated with the `view`.
         *
         * @param view The [View] with [BottomSheetBehavior].
         * @return The [BottomSheetBehavior] associated with the `view`.
         */
        fun <V : View> from(view: V): UserLockBottomSheetBehavior<V> {
            val params = view.layoutParams as? androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams
                    ?: throw IllegalArgumentException("The view is not a child of CoordinatorLayout")
            val behavior = params.behavior as? UserLockBottomSheetBehavior<*>
                    ?: throw IllegalArgumentException("The view is not associated with BottomSheetBehavior")
            return behavior as UserLockBottomSheetBehavior<V>
        }
    }
}