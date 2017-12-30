package com.twoeightnine.root.xvii.model

import com.google.gson.annotations.SerializedName

/**
 * Created by root on 10/14/16.
 */

class Video {

    val id: Int = 0
    @SerializedName("owner_id")
    val ownerId: Int = 0
    val duration: Int = 0
    val title: String? = null
    @SerializedName("photo_130")
    val photo130: String? = null
    @SerializedName("photo_320")
    val photo320: String? = null
    @SerializedName("access_key")
    val accessKey: String? = null
    val player: String? = null
    val link: String? = null

    val maxPhoto: String
        get() = photo320 ?: photo130 ?: ""

    val videoId: String
        get() = "${ownerId}_$id"
}
