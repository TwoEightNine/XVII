package com.twoeightnine.root.xvii.chats.attachments.gallery.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GalleryItem(

        val date: Long,
        val path: String,
        val type: Type,

        /**
         * duration of video
         */
        val duration: Long = 0L
) : Parcelable {

    enum class Type {
        PHOTO,
        VIDEO
    }
}