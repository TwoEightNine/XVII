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