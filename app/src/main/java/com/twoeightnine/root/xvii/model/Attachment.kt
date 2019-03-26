package com.twoeightnine.root.xvii.model

import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import com.google.gson.annotations.SerializedName
import java.io.Serializable


class Attachment : Parcelable, Serializable {

    var type: String? = null
    var photo: Photo? = null
    val sticker: Sticker? = null
    var audio: Audio? = null
    var link: Link? = null
    var video: Video? = null
    var doc: Doc? = null
    var wall: WallPost? = null
    var gift: Gift? = null

    val graffiti: Graffiti? = null

    constructor(p: Parcel) {
        type = p.readString()
        photo = p.readParcelable(Photo::class.java.classLoader) as Photo
    }

    constructor(photo: Photo) {
        this.photo = photo
        type = TYPE_PHOTO
    }

    constructor(audio: Audio) {
        this.audio = audio
        type = TYPE_AUDIO
    }

    constructor(video: Video) {
        this.video = video
        type = TYPE_VIDEO
    }

    constructor(doc: Doc) {
        this.doc = doc
        type = TYPE_DOC
    }

    constructor()

    override fun describeContents() = 0

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeString(type)
        parcel.writeParcelable(photo, Parcelable.PARCELABLE_WRITE_RETURN_VALUE)
    }

    override fun toString() =
            when (type) {
                TYPE_PHOTO -> "$type${photo!!.ownerId}_${photo!!.id}"
                TYPE_AUDIO -> "$type${audio!!.ownerId}_${audio!!.id}"
                TYPE_VIDEO -> "$type${video!!.ownerId}_${video!!.id}"
                TYPE_DOC -> "$type${doc!!.ownerId}_${doc!!.id}"
                else -> ""
            }

    class Sticker(val id: Int) {
        @SerializedName("product_id")
        val productId: Int = 0
        @SerializedName("photo_64")
        val photo64 = "https://vk.com/images/stickers/$id/64b.png"
        @SerializedName("photo_128")
        val photo128 = "https://vk.com/images/stickers/$id/128b.png"
        @SerializedName("photo_256")
        val photo256 = "https://vk.com/images/stickers/$id/256b.png"
        @SerializedName("photo_352")
        val photo352: String? = null
        @SerializedName("photo_512")
        val photo512: String? = null
        val width: Int = 0
        val height: Int = 0

        val photoMax: String
            get() {
                var maxRes: String? = photo64
                if (!TextUtils.isEmpty(photo128)) maxRes = photo128
                if (!TextUtils.isEmpty(photo256)) maxRes = photo256
                if (!TextUtils.isEmpty(photo352)) maxRes = photo352
                if (!TextUtils.isEmpty(photo512)) maxRes = photo512
                return maxRes ?: ""
            }

        override fun equals(other: Any?) = (other as? Sticker)?.id == id
    }

    class Gift {
        val id: Int = 0
        @SerializedName("thumb_256")
        val thumb256: String? = null
    }

    companion object {

        const val TYPE_PHOTO = "photo"
        const val TYPE_STICKER = "sticker"
        const val TYPE_AUDIO = "audio"
        const val TYPE_LINK = "link"
        const val TYPE_VIDEO = "video"
        const val TYPE_DOC = "doc"
        const val TYPE_WALL = "wall"
        const val TYPE_GIFT = "gift"
        const val TYPE_GRAFFITI = "graffiti"

        val CREATOR: Parcelable.Creator<Attachment> = object : Parcelable.Creator<Attachment> {
            override fun createFromParcel(parcel: Parcel): Attachment {
                return Attachment(parcel)
            }

            override fun newArray(i: Int): Array<Attachment?> {
                return arrayOfNulls(i)
            }
        }
    }
}

fun ArrayList<Attachment>.isSticker() = isNotEmpty() && this[0].type == Attachment.TYPE_STICKER

fun ArrayList<Attachment>.getPhotos() = ArrayList(this.mapNotNull { it.photo })

fun ArrayList<Attachment>.photosCount() = getPhotos().size