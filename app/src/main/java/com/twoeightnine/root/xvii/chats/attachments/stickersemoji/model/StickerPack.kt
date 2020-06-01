package com.twoeightnine.root.xvii.chats.attachments.stickersemoji.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class StickerPack(

        /**
         * name of pack or null if recent stickers
         */
        val name: String?,

        val stickers: List<Sticker>
) : Parcelable