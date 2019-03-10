package com.twoeightnine.root.xvii.views.emoji

import android.content.Context
import androidx.appcompat.widget.AppCompatEditText
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.widget.EditText
import com.twoeightnine.root.xvii.utils.EmojiHelper

class EmojiEditText : AppCompatEditText {
    private var cursorStart = -1
    private var cursorEnd = -1

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
        cursorEnd = selectionEnd
        updateText()
        setSelection(cursorStart, cursorEnd)
        cursorStart = -1
        cursorEnd = -1
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

    private fun updateText() {
        text = EmojiHelper.getEmojied(context, text.toString())
    }
}