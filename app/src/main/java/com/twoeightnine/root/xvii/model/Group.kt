package com.twoeightnine.root.xvii.model

import com.google.gson.annotations.SerializedName

/**
 * Created by root on 10/23/16.
 */

class Group() {

    constructor(groupDb: GroupDb): this() {
        this.id = groupDb.id
        this.name = groupDb.name
        this.photo100 = groupDb.photo
    }

    @SerializedName("name")
    var name: String = ""
    @SerializedName("id")
    var id: Int = 0
    @SerializedName("photo_50")
    private val photo50: String? = null
    @SerializedName("photo_100")
    var photo100: String = "http://www.iconsdb.com/icons/preview/light-gray/square-xxl.png"
    @SerializedName("photo_200")
    private val photo200: String? = null
}
