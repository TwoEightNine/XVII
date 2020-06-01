package com.twoeightnine.root.xvii.chats.attachments.stickersemoji.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "emojis")
@Parcelize
data class Emoji(

        @PrimaryKey
        val code: String,

        @ColumnInfo(name = "file_name")
        val fileName: String,

        @ColumnInfo(name = "pack_name")
        val packName: String
) : Parcelable {

    val fullPath: String
        get() = "$PATH_FMT$fileName"

    companion object {

        const val PATH_FMT = "file:///android_asset/emoji/"
    }
}