package com.twoeightnine.root.xvii.chats

import android.content.Context
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.attachments.stickers.StickersWindow
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.model.Attachment
import com.twoeightnine.root.xvii.utils.*
import com.twoeightnine.root.xvii.views.emoji.Emoji
import com.twoeightnine.root.xvii.views.emoji.EmojiKeyboard
import kotlinx.android.synthetic.main.chat_input_panel.view.*
import kotlin.math.abs

/**
 * Created by msnthrp on 17/01/18.
 */
class ChatInputController(
        private val context: Context,
        private val rootView: View,
        private val callback: ChatInputCallback
) {

    private val loadingQueue = arrayListOf<Any>()
    private val emojiKeyboard = EmojiKeyboard(rootView, context, ::addEmoji, ::onKeyboardClosed)
    private val stickerKeyboard = StickersWindow(rootView, context, ::onKeyboardClosed, callback::onStickerClicked)

    private var attachedCount = 0
    private var lastTypingInvocation = 0
    private var keyboardState = KeyboardState.TEXT

    init {
        with(rootView) {
            ivSend.setOnClickListener { callback.onSendClick() }
            ivKeyboard.setOnClickListener { switchKeyboardState() }
            ivAttach.setOnClickListener { callback.onAttachClick() }
            pbAttach.hide()
            etInput.addTextChangedListener(ChatTextWatcher())
            ivMic.setOnTouchListener(MicTouchListener())
        }
        emojiKeyboard.setSizeForSoftKeyboard()
        stickerKeyboard.setSizeForSoftKeyboard()
        setAttachedCount(0)
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
            rootView.rlAttachCount.visibility = View.GONE
            if (rootView.etInput.asText().isBlank()) {
                switchToMic()
            } else {
                switchToSend()
            }
        } else {
            rootView.rlAttachCount.show()
            val text = if (count == 10) "+" else count.toString()
            rootView.tvAttachCount.text = text
            switchToSend()
        }
    }

    private fun addEmoji(emoji: Emoji) {
        val start = rootView.etInput.selectionStart
        val end = rootView.etInput.selectionEnd
        if (start < 0) {
            rootView.etInput.append(emoji.code)
        } else {
            rootView.etInput.text?.replace(Math.min(start, end),
                    Math.max(start, end), emoji.code, 0,
                    emoji.code.length)
        }
    }

    private fun switchKeyboardState() {
        when(keyboardState) {
            KeyboardState.TEXT -> {
                keyboardState = KeyboardState.STICKERS
                stickerKeyboard.showWithRequest(rootView.etInput)
            }
            KeyboardState.STICKERS -> {
                keyboardState = KeyboardState.EMOJIS
                stickerKeyboard.dismiss()
                emojiKeyboard.showWithRequest(rootView.etInput)
            }
            KeyboardState.EMOJIS -> {
                keyboardState = KeyboardState.TEXT
                emojiKeyboard.dismiss()
            }
        }
        updateKeyboardIcon()
    }

    private fun updateKeyboardIcon() {
        val iconRes = when(keyboardState) {
            KeyboardState.TEXT -> R.drawable.ic_sticker
            KeyboardState.STICKERS -> R.drawable.ic_emoji
            KeyboardState.EMOJIS -> R.drawable.ic_keyboard
        }
        val d = ContextCompat.getDrawable(context, iconRes)
        Style.forDrawable(d, Style.DARK_TAG)
        rootView.ivKeyboard.setImageDrawable(d)
    }

    private fun invalidateProgress() {
        rootView.pbAttach.setVisible(loadingQueue.isNotEmpty())
    }

    private fun switchToSend() {
        rootView.ivSend.show()
        rootView.ivMic.hide()
    }

    private fun switchToMic() {
        rootView.ivSend.hide()
        rootView.ivMic.show()
    }

    private fun onKeyboardClosed() {
        if (emojiKeyboard.isShowing) {
            emojiKeyboard.dismiss()
        }
        if (stickerKeyboard.isShowing) {
            stickerKeyboard.dismiss()
        }
        keyboardState = KeyboardState.TEXT
        updateKeyboardIcon()
    }

    companion object {
        const val TYPING_INVOCATION_DELAY = 5 // seconds
    }

    /**
     * invokes end of recording
     * supports cancelling by swipe
     */
    private inner class MicTouchListener : View.OnTouchListener {

        private val cancelThreshold = 200
        private val delayTimer = MicClickTimer { callback.onMicPress() }

        private var xPress = 0f

        override fun onTouch(v: View?, event: MotionEvent?) = when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                xPress = event.x
                delayTimer.start()
                true
            }

            MotionEvent.ACTION_MOVE -> {
                if (shouldCancel(event)) {
                    stop(true)
                    true
                } else {
                    false
                }
            }

            MotionEvent.ACTION_UP -> {
                stop(shouldCancel(event))
                true
            }
            else -> true
        }

        private fun stop(cancel: Boolean) {
            delayTimer.cancel()
            callback.onMicRelease(cancel)
        }

        private fun shouldCancel(event: MotionEvent) = abs(xPress - event.x) > cancelThreshold
    }

    /**
     * adds delay before invoking
     */
    private inner class MicClickTimer(private val callback: () -> Unit) : CountDownTimer(150L, 150L) {
        override fun onFinish() {
            callback.invoke()
        }

        override fun onTick(millisUntilFinished: Long) {}
    }

    private inner class ChatTextWatcher : TextWatcher {

        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val text = s ?: ""

            if (text.isBlank() && attachedCount == 0) {
                switchToMic()
            } else {
                switchToSend()
            }
            if (time() - lastTypingInvocation > TYPING_INVOCATION_DELAY) {
                callback.onTypingInvoke()
                lastTypingInvocation = time()
            }
        }
    }

    /**
     * for interacting with [ChatFragment]
     */
    interface ChatInputCallback {
        fun onStickerClicked(sticker: Attachment.Sticker)
        fun onSendClick()
        fun onMicPress()
        fun onMicRelease(cancelled: Boolean)
        fun onAttachClick()
        fun onTypingInvoke()
    }

    private enum class KeyboardState {
        TEXT,
        STICKERS,
        EMOJIS
    }

}