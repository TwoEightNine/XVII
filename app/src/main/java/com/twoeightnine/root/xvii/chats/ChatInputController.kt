package com.twoeightnine.root.xvii.chats

import android.text.Editable
import android.text.TextWatcher
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
                          private val onMicClick: () -> Unit = {},
                          private val onAttachClick: () -> Unit = {}) {

    init {
        ivSend.setOnClickListener { onSendClick.invoke() }
        ivMic.setOnClickListener { onMicClick.invoke() }
        ivEmoji.setOnClickListener { onEmojiClick.invoke() }
        ivAttach.setOnClickListener { onAttachClick.invoke() }
        pbAttach.visibility = View.GONE
        setAttachedCount(0)

        etInput.addTextChangedListener(ChatTextWatcher())
    }

    fun showAttachmentLoading() {
        pbAttach.visibility = View.VISIBLE
    }

    fun hideAttachmentLoading() {
        pbAttach.visibility = View.GONE
    }

    fun setAttachedCount(count: Int) {
        if (count == 0) {
            rlAttachCount.visibility = View.GONE
        } else {
            rlAttachCount.visibility = View.VISIBLE
            val text = if (count == 10) "+" else count.toString()
            tvAttachCount.text = text
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

    private inner class ChatTextWatcher : TextWatcher {

        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (s?.length == 0) {
                ivSend.visibility = View.GONE
                ivMic.visibility = View.VISIBLE
            } else {
                ivSend.visibility = View.VISIBLE
                ivMic.visibility = View.GONE
            }
        }
    }

}