package com.twoeightnine.root.xvii.model.attachments

import android.os.Parcelable
import com.twoeightnine.root.xvii.model.WallPost
import kotlinx.android.parcel.Parcelize


@Parcelize
data class Attachment(
        var type: String? = null,
        var photo: Photo? = null,
        val sticker: Sticker? = null,
        var audio: Audio? = null,
        var link: Link? = null,
        var video: Video? = null,
        var doc: Doc? = null,
        var wall: WallPost? = null,
        var gift: Gift? = null
) : Parcelable {

    constructor(photo: Photo) : this(
            type = TYPE_PHOTO,
            photo = photo
    )

    constructor(audio: Audio) : this(
            type = TYPE_AUDIO,
            audio = audio
    )

    constructor(video: Video) : this(
            type = TYPE_VIDEO,
            video = video
    )

    constructor(doc: Doc) : this(
            doc = doc,
            type = TYPE_DOC
    )

    override fun toString() = (when (type) {
        TYPE_PHOTO -> photo
        TYPE_AUDIO -> audio
        TYPE_VIDEO -> video
        TYPE_DOC -> doc
        else -> null
    } as? IdTypeable)?.getId() ?: "null"

    companion object {

        const val TYPE_PHOTO = "photo"
        const val TYPE_STICKER = "sticker"
        const val TYPE_AUDIO = "audio"
        const val TYPE_LINK = "link"
        const val TYPE_VIDEO = "video"
        const val TYPE_DOC = "doc"
        const val TYPE_WALL = "wall"
        const val TYPE_GIFT = "gift"
    }
}

fun ArrayList<Attachment>.isSticker() = isNotEmpty() && this[0].sticker != null

fun ArrayList<Attachment>.getPhotos() = ArrayList(this.mapNotNull { it.photo })

fun ArrayList<Attachment>.photosCount() = getPhotos().size