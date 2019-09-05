package com.twoeightnine.root.xvii.chatowner.model.api

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.twoeightnine.root.xvii.model.Conversation
import com.twoeightnine.root.xvii.model.User

data class ConversationsResponse(

        @SerializedName("items")
        @Expose
        val items: ArrayList<Conversation> = arrayListOf(),

        @SerializedName("profiles")
        @Expose
        val profiles: ArrayList<User> = arrayListOf()
)