package com.twoeightnine.root.xvii.network.response

import com.twoeightnine.root.xvii.model.Group
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.model.WallPost

/**
 * Created by root on 1/27/17.
 */

class WallPostResponse {
    val items: MutableList<WallPost> = mutableListOf()
    val groups: MutableList<Group> = mutableListOf()
    val profiles: MutableList<User> = mutableListOf()
}
