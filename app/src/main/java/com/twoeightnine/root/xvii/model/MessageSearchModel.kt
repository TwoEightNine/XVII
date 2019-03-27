package com.twoeightnine.root.xvii.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.twoeightnine.root.xvii.utils.asChatPeerId

class MessageSearchModel {

    private val id: Int = 0
    private val type: String? = null
    private val title: String? = null
    private val name: String? = null
    @SerializedName("first_name")
    @Expose
    private val firstName: String? = null
    @SerializedName("last_name")
    @Expose
    private val lastName: String? = null
    @SerializedName("photo_100")
    @Expose
    private val photo100: String? = null

    val message: Message
        get() {
            val mess = Message()
            mess.photo = photo100
            when (type) {
                PAGE, GROUP -> {
                    mess.title = name
                    mess.userId = -id
                }

                PROFILE -> {
                    mess.title = "$firstName $lastName"
                    mess.userId = id
                }

                CHAT -> {
                    mess.userId = id.asChatPeerId()
                    mess.chatId = id
                    mess.title = title
                }
            }
            return mess
        }

    companion object {

        const val PAGE = "page"
        const val GROUP = "group"
        const val PROFILE = "profile"
        const val CHAT = "chat"
    }

}
