package com.twoeightnine.root.xvii.journal.message.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MessageInfo(
        val messageId: Int,

        val peerId: Int,
        val peerName: String,

        val events: List<MessageEvent>,

        val fromId : Int = peerId,
        val fromName : String = peerName
) : Parcelable