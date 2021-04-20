package com.twoeightnine.root.xvii.background.music.models

import android.os.Parcelable
import com.twoeightnine.root.xvii.model.attachments.Audio
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Track(
        val audio: Audio,
        val cachePath: String? = null
) : Parcelable {

    fun isCached() = !cachePath.isNullOrEmpty()

    override fun equals(other: Any?) = audio == (other as? Track)?.audio

    override fun hashCode(): Int {
        var result = audio.hashCode()
        result = 31 * result + (cachePath?.hashCode() ?: 0)
        return result
    }
}