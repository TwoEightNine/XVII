package com.twoeightnine.root.xvii.utils

import android.content.Context
import com.twoeightnine.root.xvii.model.attachments.Attachment

class AttachUtils(
        private val context: Context,
        private val listener: ((Int) -> Unit)?) {

    val attachments: MutableList<Attachment> = mutableListOf()
    var forwarded = ""
        set(value) {
            field = value
            updateCounter()
        }

    fun add(attachment: Attachment) {
        if (count < 10) {
            attachments.add(attachment)
            updateCounter()
        } else {
//            showError(context, R.string.ten_attachments)
        }
    }

    private fun updateCounter() {
        listener?.invoke(count)
    }

    fun clear() {
        attachments.clear()
        forwarded = ""
        updateCounter()
    }

    val count: Int
        get() = attachments.size + if (forwarded.isEmpty()) 0 else 1

    fun remove(removed: MutableList<Attachment>) {
        for (att in removed) {
            attachments.remove(att)
        }
        updateCounter()
    }

    fun asString() = attachments
            .map { it.toString() }
            .joinToString(separator = ",")
}