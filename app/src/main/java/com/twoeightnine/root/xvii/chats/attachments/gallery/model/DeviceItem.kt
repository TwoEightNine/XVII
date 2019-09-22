package com.twoeightnine.root.xvii.chats.attachments.gallery.model

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DeviceItem(

        /**
         * modification date
         * the field these elements are sorted by
         */
        val date: Long,

        /**
         * path to file
         */
        val path: String,

        /**
         * type, photo, doc or video
         */
        val type: Type,

        /**
         * duration of video
         */
        val duration: Long = 0L,

        /**
         * path to thumbnail
         */
        var thumbnail: Bitmap? = null
) : Parcelable {

    enum class Type {
        PHOTO,
        VIDEO,
        DOC
    }
}