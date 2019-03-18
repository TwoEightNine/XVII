package com.twoeightnine.root.xvii.lg

import android.os.Parcelable
import com.twoeightnine.root.xvii.utils.time
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LgEvent(
        val text: String,
        val type: Type = Type.INFO,
        val ts: Int = time()
) : Parcelable {

    enum class Type {
        INFO,
        ERROR
    }
}