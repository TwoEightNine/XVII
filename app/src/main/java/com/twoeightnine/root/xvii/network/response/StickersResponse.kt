package com.twoeightnine.root.xvii.network.response

import com.google.gson.annotations.SerializedName
import com.twoeightnine.root.xvii.model.StickerMind

/**
 * Created by root on 3/24/17.
 */

class StickersResponse {

    @SerializedName("base_url")
    val baseUrl: String? = null
    val count: Int = 0
    val dictionary: MutableList<StickerMind>? = null
}
