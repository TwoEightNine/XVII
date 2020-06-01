package com.twoeightnine.root.xvii.chats.attachments.stickersemoji.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "sticker_usages")
@Parcelize
data class StickerUsage(

        @PrimaryKey
        @ColumnInfo(name = "sticker_id")
        val stickerId: Int,

        @ColumnInfo(name = "last_used")
        val lastUsed: Int
) : Parcelable