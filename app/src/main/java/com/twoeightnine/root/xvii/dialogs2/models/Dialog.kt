package com.twoeightnine.root.xvii.dialogs2.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "dialogs")
data class Dialog(
        @PrimaryKey
        val peerId: Int = 0,
        val title: String = "",
        val photo: String? = null,
        val text: String = "",
        val timeStamp: Int = 0,
        val isOut: Boolean = false,
        val isRead: Boolean = true,
        val unreadCount: Int = 0
) : Parcelable