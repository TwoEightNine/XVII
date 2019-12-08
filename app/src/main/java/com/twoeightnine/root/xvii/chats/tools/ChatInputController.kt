package com.twoeightnine.root.xvii.chats.tools

import android.content.ClipDescription
import android.content.Context
import android.net.Uri
import android.os.CountDownTimer
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.webkit.MimeTypeMap
import androidx.core.content.ContextCompat
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.attachments.stickers.StickersStorage
import com.twoeightnine.root.xvii.chats.attachments.stickers.StickersWindow
import com.twoeightnine.root.xvii.lg.Lg
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.model.attachments.Sticker
import com.twoeightnine.root.xvii.utils.*
import com.twoeightnine.root.xvii.views.emoji.Emoji
import com.twoeightnine.root.xvii.views.emoji.EmojiKeyboard
import kotlinx.android.synthetic.main.chat_input_panel.view.*
import java.io.File
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


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
    private val voiceRecorder = VoiceRecorder(context, callback)
    private val stickers = StickersStorage(context, StickersStorage.Type.AVAILABLE).readFromFile()

    private var attachedCount = 0
    private var lastTypingInvocation = 0
    private var keyboardState = KeyboardState.TEXT

    init {
        with(rootView) {
            ivSend.setOnClickListener { callback.onSendClick() }
            ivKeyboard.setOnClickListener { switchKeyboardState() }
            ivKeyboard.setVisible(Prefs.showStickers)
            ivAttach.setOnClickListener { callback.onAttachClick() }
            pbAttach.hide()
            etInput.addTextChangedListener(ChatTextWatcher())
            etInput.onRichContentAdded = ::onRichContentAdded
            when {
                Prefs.sendByEnter -> {
                    etInput.imeOptions = EditorInfo.IME_ACTION_SEND
                    etInput.inputType = EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES
                    etInput.setOnEditorActionListener { _, actionId, _ ->
                        if (actionId == EditorInfo.IME_ACTION_SEND && Prefs.sendByEnter) {
                            callback.onSendClick()
                            true
                        } else {
                            false
                        }
                    }
                }
                !Prefs.lowerTexts -> {
                    etInput.inputType = etInput.inputType or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                }
            }
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
            if (rootView.etInput.asText().isBlank() && Prefs.showVoice) {
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
            rootView.etInput.text?.replace(min(start, end),
                    max(start, end), emoji.code, 0,
                    emoji.code.length)
        }
    }

    private fun switchKeyboardState() {
        when (keyboardState) {
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
        val iconRes = when (keyboardState) {
            KeyboardState.TEXT -> R.drawable.ic_sticker
            KeyboardState.STICKERS -> R.drawable.ic_emoji
            KeyboardState.EMOJIS -> R.drawable.ic_keyboard
        }
        val d = ContextCompat.getDrawable(context, iconRes)
//        d?.stylize(ColorManager.DARK_TAG)
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

    private fun onRichContentAdded(uri: Uri, description: ClipDescription) {
        if (description.mimeTypeCount > 0) {
            val fileExtension = MimeTypeMap.getSingleton()
                    .getExtensionFromMimeType(description.getMimeType(0)) ?: return

            val richContentFile = File(context.cacheDir, "richContent.$fileExtension")
            if (!writeToFileFromContentUri(context, richContentFile, uri)) {
                Lg.wtf("error adding rich content")
            } else {
                callback.onRichContentAdded(richContentFile.absolutePath)
            }
        }
    }

    private fun getMatchedStickers(typed: CharSequence): List<Sticker> {
        if (typed.isBlank() || typed.length < 2 && !EmojiHelper.hasEmojis(typed.toString())) {
            return arrayListOf()
        }

        return stickers
                .filter { sticker ->
                    sticker.keywords
                            .map { word -> if (word.startsWith(typed)) 1 else 0 }
                            .sum() != 0
                }
    }

    companion object {
        const val TYPING_INVOCATION_DELAY = 5 // seconds
    }

    /**
     * invokes end of recording
     * supports cancelling by swipe
     */
    private inner class MicTouchListener : View.OnTouchListener {

        /**
         * threshold to cancel
         */
        private val cancelThreshold = 200

        /**
         * threshold to lock
         */
        private val lockThreshold = 300
        private val delayTimer = MicClickTimer {
            if (callback.hasMicPermissions()) {
                voiceRecorder.startRecording()
            }
        }

        /**
         * to watch if cancelled
         */
        private var xPress = 0f

        /**
         * to watch if locked
         */
        private var yPress = 0f
        private var alreadyStopped = false

        /**
         * lock flag
         */
        private var locked = false

        override fun onTouch(v: View?, event: MotionEvent?) = when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                xPress = event.x
                yPress = event.y
                if (locked) {
                    stop(false)
                    false
                } else {
                    delayTimer.start()
                    alreadyStopped = false
                    locked = false
                    true
                }
            }

            MotionEvent.ACTION_MOVE -> {
                when {
                    shouldLock(event) -> {
                        locked = true
                        callback.onVoiceRecorderLocked()
                        false
                    }
                    shouldCancel(event) -> {
                        stop(true)
                        true
                    }
                    else -> false
                }
            }

            MotionEvent.ACTION_UP -> {
                if (!alreadyStopped && !locked) {
                    stop(false)
                }
                true
            }
            else -> true
        }

        private fun stop(cancel: Boolean) {
            alreadyStopped = true
            delayTimer.cancel()
            voiceRecorder.stopRecording(cancel)
        }

        private fun shouldCancel(event: MotionEvent) = abs(xPress - event.x) > cancelThreshold

        // disable for now. guess how to cancel from lock
        private fun shouldLock(event: MotionEvent) = false // abs(yPress - event.y) > lockThreshold
    }

    /**
     * adds delay before invoking
     */
    private inner class MicClickTimer(private val callback: () -> Unit)
        : CountDownTimer(150L, 150L) {
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

            if (text.isBlank() && attachedCount == 0 && Prefs.showVoice) {
                switchToMic()
            } else {
                switchToSend()
            }
            callback.onStickersSuggested(getMatchedStickers(text))

            val delayExceed = time() - lastTypingInvocation > TYPING_INVOCATION_DELAY
            if (delayExceed && text.isNotBlank()) {
                callback.onTypingInvoke()
                lastTypingInvocation = time()
            }
        }
    }

    /**
     * for interacting with [ChatFragment]
     */
    interface ChatInputCallback : VoiceRecorder.RecorderCallback {
        fun onVoiceRecorderLocked()
        fun onStickerClicked(sticker: Sticker)
        fun onSendClick()
        fun hasMicPermissions(): Boolean
        fun onAttachClick()
        fun onTypingInvoke()
        fun onRichContentAdded(filePath: String)
        fun onStickersSuggested(stickers: List<Sticker>)
    }

    private enum class KeyboardState {
        TEXT,
        STICKERS,
        EMOJIS
    }

}