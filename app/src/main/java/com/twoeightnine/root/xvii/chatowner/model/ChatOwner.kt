package com.twoeightnine.root.xvii.chatowner.model

import android.content.Context
import com.twoeightnine.root.xvii.model.Conversation
import com.twoeightnine.root.xvii.model.Group
import com.twoeightnine.root.xvii.model.User

/**
 * represents an instance that can have a chat: [User], [Conversation] or [Group]
 */
interface ChatOwner {

    fun getPeerId(): Int
    fun getAvatar(): String?
    fun getTitle(): String
    fun getInfoText(context: Context): String

}