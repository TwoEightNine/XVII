package com.twoeightnine.root.xvii.chatowner.model.api

import com.google.gson.annotations.SerializedName
import com.twoeightnine.root.xvii.model.User

data class MembersResponse(

        @SerializedName("profiles")
        val profiles: List<User>
)