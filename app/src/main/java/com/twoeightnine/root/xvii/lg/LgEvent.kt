package com.twoeightnine.root.xvii.lg

import android.os.Parcelable
import com.twoeightnine.root.xvii.utils.time
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LgEvent(
        val text: String,
        val tag: String? = null,
        val throwable: Throwable? = null,
        val warn: Boolean = false,
        val ts: Int = time()
) : Parcelable