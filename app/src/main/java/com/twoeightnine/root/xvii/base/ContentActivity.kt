/*
 * xvii - messenger for vk
 * Copyright (C) 2021  TwoEightNine
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.twoeightnine.root.xvii.base

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.utils.DragTouchListener
import kotlinx.android.synthetic.main.activity_content.*

/**
 * it is often needed to place the only fragment inside an activity
 * so this activity is for this case!
 * just extend it and override [createFragment]
 */
abstract class ContentActivity : BaseActivity() {

    protected open fun getLayoutId() = R.layout.activity_content

    abstract fun createFragment(intent: Intent?): Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())
        savedInstanceState ?: loadFragment(createFragment(intent))

        if (shouldEnableSwipeToBack() && Prefs.enableSwipeToBack) {
            (vDraggable.layoutParams as? ViewGroup.MarginLayoutParams)
                    ?.bottomMargin = getDraggableBottomMargin()
            vDraggable.setOnTouchListener(DragTouchListener(this, flContainer, vShadow))
        }

        //android O fix bug orientation. https://stackoverflow.com/a/50832408
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        // https://medium.com/androiddevelopers/windows-insets-fragment-transitions-9024b239a436
        flContainer.setOnApplyWindowInsetsListener { view, insets ->
            var consumed = false

            val vg = view as ViewGroup
            val childCount = vg.childCount
            for (i in 0 until childCount) {

                // Dispatch the insets to the child
                val childResult = vg.getChildAt(i).dispatchApplyWindowInsets(insets)
                // If the child consumed the insets, record it
                if (childResult.isConsumed) {
                    consumed = true
                }
            }
            // If any of the children consumed the insets, return
            // an appropriate value
            if (consumed) insets.consumeSystemWindowInsets() else insets
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.apply { loadFragment(createFragment(this)) }
    }

    override fun getThemeId() = R.style.AppTheme_Transparent

    protected fun loadFragment(fragment: Fragment) {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.flContainer, fragment)
                .commitAllowingStateLoss()
    }

    protected fun getFragment() = supportFragmentManager.findFragmentById(R.id.flContainer)

    protected open fun shouldEnableSwipeToBack() = true

    protected open fun getDraggableBottomMargin() = 0

}