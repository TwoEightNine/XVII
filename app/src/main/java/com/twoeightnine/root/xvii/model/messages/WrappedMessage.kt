package com.twoeightnine.root.xvii.model.messages

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class WrappedMessage(
        val message: Message,
        val sent: Boolean = true,

        /**
         * used only if [sent] == true
         */
        val hasAttachmentsOrForwarded: Boolean = false
) : Parcelable