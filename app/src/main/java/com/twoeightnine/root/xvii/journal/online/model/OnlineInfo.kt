package com.twoeightnine.root.xvii.journal.online.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class OnlineInfo(
        val userId: Int,
        val userName: String,
        val events: List<OnlineEvent>
) : Parcelable