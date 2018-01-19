package com.twoeightnine.root.xvii.chats

import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.widget.*
import com.twoeightnine.root.xvii.views.emoji.Emoji

/**
 * Created by msnthrp on 17/01/18.
 */
class ChatInputController(private val ivSend: ImageView,
                          private val ivMic: ImageView,
                          private val ivAttach: ImageView,
                          private val pbAttach: ProgressBar,
                          private val rlAttachCount: RelativeLayout,
                          private val tvAttachCount: TextView,
                          private val ivEmoji: ImageView,
                          private val etInput: EditText,
                          private val onEmojiClick: () -> Unit = {},
                          private val onSendClick: () -> Unit = {},
                          private val onMicPress: () -> Unit = {},
                          private val onMicRelease: (Boolean) -> Unit = {},
                          private val onAttachClick: () -> Unit = {}) {

    private var attachedCount = 0
    private val loadingQueue = arrayListOf<Any>()

    init {
        ivSend.setOnClickListener { onSendClick.invoke() }
        ivEmoji.setOnClickListener { onEmojiClick.invoke() }
        ivAttach.setOnClickListener { onAttachClick.invoke() }
        pbAttach.visibility = View.GONE
        setAttachedCount(0)

        etInput.addTextChangedListener(ChatTextWatcher())
        ivMic.setOnTouchListener(MicTouchListener())
    }

    fun addItemAsBeingLoaded(item: Any) {
        loadingQueue.add(item)
        invalidateProgress()
    }

    fun removeItemAsLoaded(item: Any) {
        loadingQueue.remove(item)
        invalidateProgress()
    }

    fun setAttachedCount(count: Int) {
        attachedCount = count
        if (count == 0) {
            rlAttachCount.visibility = View.GONE
            if (etInput.text.toString().isBlank()) {
                switchToMic()
            } else {
                switchToSend()
            }
        } else {
            rlAttachCount.visibility = View.VISIBLE
            val text = if (count == 10) "+" else count.toString()
            tvAttachCount.text = text
            switchToSend()
        }
    }

    fun addEmoji(emoji: Emoji) {
        val start = etInput.selectionStart
        val end = etInput.selectionEnd
        if (start < 0) {
            etInput.append(emoji.code)
        } else {
            etInput.text.replace(Math.min(start, end),
                    Math.max(start, end), emoji.code, 0,
                    emoji.code.length)
        }
    }

    private fun invalidateProgress() {
        if (loadingQueue.isEmpty()) {
            pbAttach.visibility = View.GONE
        } else {
            pbAttach.visibility = View.VISIBLE
        }
    }

    private fun switchToSend() {
        ivSend.visibility = View.VISIBLE
        ivMic.visibility = View.GONE
    }

    private fun switchToMic() {
        ivSend.visibility = View.GONE
        ivMic.visibility = View.VISIBLE
    }

    private inner class MicTouchListener : View.OnTouchListener {

        private val cancelThreshold = 200
        private val delayTimer = MicClickTimer { onMicPress.invoke() }

        private var xPress = 0f

        override fun onTouch(v: View?, event: MotionEvent?)
             = when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    xPress = event.x
                    delayTimer.start()
                    true
                }
                MotionEvent.ACTION_UP -> {
                    delayTimer.cancel()
                    val diff = xDiff(event)
                    onMicRelease.invoke(diff <= cancelThreshold)
                    true
                }
                else -> true
            }

        private fun xDiff(event: MotionEvent) = xPress - event.x
    }

    private inner class MicClickTimer(private val callback: () -> Unit): CountDownTimer(150L, 150L) {
        override fun onFinish() {
            callback.invoke()
        }

        override fun onTick(millisUntilFinished: Long) {}
    }

    private inner class ChatTextWatcher : TextWatcher {

        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (s?.isBlank() ?: true && attachedCount == 0) {
                switchToMic()
            } else {
                switchToSend()
            }
        }
    }

}