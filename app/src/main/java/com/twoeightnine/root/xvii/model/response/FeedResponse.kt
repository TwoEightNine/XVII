package com.twoeightnine.root.xvii.response

import com.google.gson.annotations.SerializedName
import com.twoeightnine.root.xvii.model.Group
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.model.WallPost

/**
 * Created by root on 12/31/16.
 */

class FeedResponse {

    val items: MutableList<WallPost>? = null
    val groups: MutableList<Group>? = null
    val profiles: MutableList<User> ? = null
    @SerializedName("next_from")
    val nextFrom = ""
}
