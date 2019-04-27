package com.twoeightnine.root.xvii.model.attachments

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.twoeightnine.root.xvii.crypto.CryptoEngine
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 * Created by root on 10/14/16.
 */

@Parcelize
data class Doc(
        val id: Int = 0,
        @SerializedName("owner_id")
        val ownerId: Int = 0,
        val title: String? = null,
        @SerializedName("access_key")
        val accessKey: String? = null,
        val ext: String? = null,
        val url: String? = null,
        val size: Int = 0,
        val type: Int = 0,

        //for voice messages and gifs
        val preview: Preview? = null
) : Parcelable, IdTypeable {

    val isGif: Boolean
        get() = type == 3 &&
                ext == "gif"

    val isGraffiti: Boolean
        get() = type == 4 &&
                ext == "png" &&
                preview != null &&
                preview.graffiti != null

    val isVoiceMessage: Boolean
        get() = type == 5 &&
                preview != null &&
                preview.audioMsg != null

    val isEncrypted: Boolean
        get() = ".$ext" == CryptoEngine.EXTENSION

    override fun getId() = "doc${ownerId}_$id"

    override fun toString() = "{id: $id, title: $title, ext: $ext, type: $type, size: $size, url: $url}"

    fun getPreview() = preview?.photo?.sizes?.get(0)?.src ?: ""
}

@Parcelize
data class Preview(

        @SerializedName("audio_msg")
        @Expose
        val audioMsg: AudioMsg? = null,

        @SerializedName("photo")
        @Expose
        val photo: PhotoPreview? = null,

        @SerializedName("graffiti")
        @Expose
        val graffiti: Graffiti? = null
) : Parcelable

@Parcelize
data class AudioMsg(

        @SerializedName("duration")
        @Expose
        val duration: Int = 0,

        @SerializedName("waveform")
        @Expose
        val waveform: ArrayList<Int>? = null,

        @SerializedName("link_mp3")
        val linkMp3: String? = null
) : Parcelable

@Parcelize
data class PhotoPreview(

        @SerializedName("sizes")
        @Expose
        val sizes: List<PreviewSize> = arrayListOf()
) : Parcelable

@Parcelize
data class PreviewSize(

        @SerializedName("src")
        @Expose
        val src: String? = null
) : Parcelable

@Parcelize
data class Graffiti(

        @SerializedName("src")
        @Expose
        val src: String? = null
) : Parcelable
