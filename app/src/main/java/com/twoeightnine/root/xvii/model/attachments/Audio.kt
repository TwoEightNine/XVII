package com.twoeightnine.root.xvii.model.attachments

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.twoeightnine.root.xvii.utils.secToTime
import kotlinx.android.parcel.Parcelize

/**
 * Created by root on 9/27/16.
 */

@Parcelize
data class Audio(

        @SerializedName("id")
        @Expose
        val id: Int = 0,

        @SerializedName("owner_id")
        @Expose
        val ownerId: Int = 0,

        @SerializedName("artist")
        @Expose
        val artist: String? = null,

        @SerializedName("title")
        @Expose

        val title: String? = null,

        @SerializedName("duration")
        @Expose
        val duration: Int = 0,

        @SerializedName("url")
        @Expose
        private val _url: String? = null
) : Parcelable, IdTypeable {

    val url: String?
        get() = try {
            _url?.substring(0, _url.indexOf("?extra")) ?: ""
        } catch (e: Exception) {
            _url ?: ""
        }

    val fullId: String
        get() = "${id}_$ownerId"

    override fun getId() = "audio$fullId"

    constructor(audioMsg: AudioMsg, title: String) : this(
            duration = audioMsg.duration,
            _url = audioMsg.linkMp3,
            title = title,
            artist = secToTime(audioMsg.duration)
    )
}
