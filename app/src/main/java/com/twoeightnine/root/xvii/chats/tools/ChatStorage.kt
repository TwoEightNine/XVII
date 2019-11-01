package com.twoeightnine.root.xvii.chats.tools

import android.content.Context

class ChatStorage(context: Context) {

    private val prefs = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    /**
     * message of user that was not sent
     */
    fun getMessageText(peerId: Int): String? = prefs.getString(nameFor(peerId), null)

    fun setMessageText(peerId: Int, value: String) = prefs.edit().putString(nameFor(peerId), value).apply()

    private fun nameFor(peerId: Int) = "$MESSAGE_TEXT$peerId"

    companion object {

        private const val NAME = "chatStorage"
        private const val MESSAGE_TEXT = "messageText"
    }
}