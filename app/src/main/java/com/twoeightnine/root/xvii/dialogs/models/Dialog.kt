package com.twoeightnine.root.xvii.dialogs.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "dialogs")
data class Dialog(
        @PrimaryKey
        val peerId: Int = 0,
        var messageId: Int = 0,
        val title: String = "",
        val photo: String? = null,
        var text: String = "",
        var timeStamp: Int = 0,
        var isOut: Boolean = false,
        var isRead: Boolean = true,
        var unreadCount: Int = 0,
        var isOnline: Boolean = false,
        var isMute: Boolean = false
) : Parcelable