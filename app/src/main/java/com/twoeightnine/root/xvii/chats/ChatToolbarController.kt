package com.twoeightnine.root.xvii.chats

import androidx.appcompat.widget.Toolbar
import com.twoeightnine.root.xvii.utils.hide
import com.twoeightnine.root.xvii.utils.load
import com.twoeightnine.root.xvii.utils.show
import com.twoeightnine.root.xvii.utils.time
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
        actionSubscription?.unsubscribe()
        hide()
    }

    private fun startTimer() {
        actionSubscription?.unsubscribe()
        lastAction = time()
        actionSubscription = Completable.timer(ACTION_DELAY, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .subscribe {
                    if (time() - lastAction >= ACTION_DELAY) {
                        hide()
                    }
                }
    }

    private fun hide() {
        with(toolbar) {
            tvSubtitle.show()
            tvRecordingVoice.hide()
            tvTyping.hide()
        }
    }

    companion object {
        private const val ACTION_DELAY = 5L
    }
}