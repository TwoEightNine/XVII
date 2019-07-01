package com.twoeightnine.root.xvii.chats.tools

import androidx.appcompat.widget.Toolbar
import com.twoeightnine.root.xvii.lg.Lg
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.utils.*
import kotlinx.android.synthetic.main.toolbar_chat.view.*
import rx.Completable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

class ChatToolbarController(private val toolbar: Toolbar) {

    private var lastAction = 0
    private var actionSubscription: Subscription? = null

    fun setTitle(title: String) {
        toolbar.tvTitle.text = title
        if (Prefs.lowerTexts) toolbar.tvTitle.lower()
    }

    fun setSubtitle(subtitle: String) {
        toolbar.tvSubtitle.text = subtitle
    }

    fun setAvatar(photo: String?) {
        toolbar.ivAvatar.load(photo)
    }

    fun showTyping() {
        with(toolbar) {
            tvTyping.show()
            tvRecordingVoice.hide()
            tvSubtitle.hide()
        }
        startTimer()
    }

    fun showRecording() {
        with(toolbar) {
            tvRecordingVoice.show()
            tvTyping.hide()
            tvSubtitle.hide()
        }
        startTimer()
    }

    fun hideActions() {
        l("hide actions")
        actionSubscription?.unsubscribe()
        hide()
    }

    private fun startTimer() {
        l("start timer")
        actionSubscription?.unsubscribe()
        lastAction = time()
        actionSubscription = Completable.timer(ACTION_DELAY_MS, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribe {
                    l("time fired: $lastAction ${time()}")
                    if (time() - lastAction >= ACTION_DELAY_S) {
                        hide()
                    }
                }
    }

    private fun hide() {
        with(toolbar) {
            l("hide")
            tvSubtitle.show()
            tvRecordingVoice.hide()
            tvTyping.hide()
        }
    }

    private fun l(s: String) {
        Lg.i("[toolbar] $s")
    }

    companion object {
        private const val ACTION_DELAY_MS = 5500L
        private const val ACTION_DELAY_S = 5
    }
}