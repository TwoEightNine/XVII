package com.twoeightnine.root.xvii.model.attachments

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Sticker(

        @SerializedName("id")
        @Expose
        val id: Int = 0,

        @SerializedName("product_id")
        @Expose
        val productId: Int = 0
) : Parcelable {

    override fun equals(other: Any?) = (other as? Sticker)?.id == id

    override fun hashCode() = id

    val photo256: String
        get() = String.format(URL_256_FMT, id)

    val photo512: String
        get() = String.format(URL_512_FMT, id)

    companion object {

        const val URL_512_FMT = "https://vk.com/sticker/1-%d-512b"
        const val URL_256_FMT = "https://vk.com/sticker/1-%d-256b"
    }
}