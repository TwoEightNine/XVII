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

package com.twoeightnine.root.xvii.chats.tools

import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.extensions.getInitials
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.uikit.XviiToolbar
import com.twoeightnine.root.xvii.utils.time
import global.msnthrp.xvii.uikit.extensions.hide
import global.msnthrp.xvii.uikit.extensions.lowerIf
import global.msnthrp.xvii.uikit.extensions.setVisibleWithInvis
import global.msnthrp.xvii.uikit.extensions.show
import kotlinx.android.synthetic.main.toolbar2.view.*
import rx.Completable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

class ChatToolbarController(private val xviiToolbar: XviiToolbar) {

    private var lastAction = 0
    private var actionSubscription: Subscription? = null

    fun setData(title: String, photo: String?, id: Int = 0) {
        xviiToolbar.tvChatTitle.text = title
        xviiToolbar.tvChatTitle.lowerIf(Prefs.lowerTexts)
        if (id != -App.GROUP) {
            xviiToolbar.civAvatar.load(photo, title.getInitials(), id = id)
        }
    }

    @Deprecated("Use setData")
    fun setTitle(title: String) {
        xviiToolbar.tvChatTitle.text = title
        xviiToolbar.tvChatTitle.lowerIf(Prefs.lowerTexts)
    }

    fun setSubtitle(subtitle: CharSequence) {
        xviiToolbar.tvSubtitle.text = subtitle
    }

    @Deprecated("Use setData")
    fun setAvatar(photo: String?) {
        xviiToolbar.civAvatar.load(photo)
    }

    fun showActivity() {
        with(xviiToolbar) {
            typingView.show()
            tvSubtitle.setVisibleWithInvis(false)
        }
        startTimer()
    }

    fun hideActions() {
        actionSubscription?.unsubscribe()
        hide()
    }

    @Suppress("UnstableApiUsage")
    private fun startTimer() {
        actionSubscription?.unsubscribe()
        lastAction = time()
        actionSubscription = Completable.timer(ACTION_DELAY_MS, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribe {
                    if (time() - lastAction >= ACTION_DELAY_S) {
                        hide()
                    }
                }
    }

    private fun hide() {
        with(xviiToolbar) {
            tvSubtitle.show()
            typingView.hide()
        }
    }

    companion object {
        private const val ACTION_DELAY_MS = 5500L
        private const val ACTION_DELAY_S = 5
    }
}