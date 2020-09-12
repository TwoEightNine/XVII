package com.twoeightnine.root.xvii.views.emoji

import android.content.ClipDescription
import android.content.Context
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.N_MR1
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.inputmethod.EditorInfoCompat
import androidx.core.view.inputmethod.InputConnectionCompat
import com.twoeightnine.root.xvii.lg.L
import com.twoeightnine.root.xvii.utils.EmojiHelper


class EmojiEditText : AppCompatEditText {

    var onRichContentAdded: ((Uri, ClipDescription) -> Unit)? = null

    private var cursorStart = -1
    private var emojisBefore = 0

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    override fun onTextChanged(text: CharSequence, start: Int, lengthBefore: Int, lengthAfter: Int) {

    }

    fun onChanged() {
        if (cursorStart != -1) return

        cursorStart = selectionStart
        val end = selectionEnd
        text = EmojiHelper.getEmojied(context, text.toString())
        setSelection(cursorStart, end)
        cursorStart = -1
    }

    fun init() {
        addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (EmojiHelper.hasEmojis(text.toString()) &&
                        emojisBefore < EmojiHelper.getEmojisCount(text.toString())) {
                    onChanged()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                emojisBefore = if (EmojiHelper.hasEmojis(s.toString())) {
                    EmojiHelper.getEmojisCount(s.toString())
                } else {
                    0
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }

    override fun onCreateInputConnection(editorInfo: EditorInfo): InputConnection {
        val ic = super.onCreateInputConnection(editorInfo)
        EditorInfoCompat.setContentMimeTypes(editorInfo, MIME_TYPES)
        return InputConnectionCompat.createWrapper(ic, editorInfo,
                InputConnectionCompat.OnCommitContentListener { inputContentInfo, flags, opts ->
                    if (SDK_INT >= N_MR1 && (flags and InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION != 0)) {
                        try {
                            onRichContentAdded?.apply {
                                inputContentInfo.requestPermission()
                                invoke(
                                        inputContentInfo.contentUri,
                                        inputContentInfo.description
                                )
                                inputContentInfo.releasePermission()
                            }
                        } catch (e: Exception) {
                            L.tag("rich content")
                                    .throwable(e)
                                    .log("error accepting content")
                            return@OnCommitContentListener false
                        }
                    }
                    true
                })
    }

    companion object {
        private val MIME_TYPES = arrayOf("image/*", "image/png", "image/gif", "image/jpeg")
    }
}