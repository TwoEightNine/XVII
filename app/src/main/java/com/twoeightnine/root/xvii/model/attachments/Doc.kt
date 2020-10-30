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

        @SerializedName("id")
        @Expose
        val id: Int = 0,

        @SerializedName("owner_id")
        @Expose
        val ownerId: Int = 0,

        @SerializedName("title")
        @Expose
        val title: String? = null,

        @SerializedName("access_key")
        @Expose
        val accessKey: String? = null,

        @SerializedName("ext")
        @Expose
        val ext: String? = null,

        @SerializedName("url")
        @Expose
        val url: String? = null,

        @SerializedName("size")
        @Expose
        val size: Int = 0,

        @SerializedName("type")
        @Expose
        val type: Int = 0,

        //for voice messages and gifs
        @SerializedName("preview")
        @Expose
        val preview: Preview? = null
) : Parcelable, IdTypeable {

    val isGif: Boolean
        get() = type == 3 &&
                ext == "gif"

    val isVoiceMessage: Boolean
        get() = type == 5 &&
                preview != null &&
                preview.audioMsg != null

    val isEncrypted: Boolean
        get() = ".$ext" == CryptoEngine.EXTENSION

    override fun getId() = "doc${ownerId}_$id"
}

@Parcelize
data class Preview(

        @SerializedName("audio_msg")
        @Expose
        val audioMsg: AudioMsg? = null,

        @SerializedName("photo")
        @Expose
        val photo: PhotoPreview? = null
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
) : Parcelable {

        fun getSmallPreview(): PreviewSize? = sizes.minByOrNull { it.height }
}

@Parcelize
data class PreviewSize(

        @SerializedName("src")
        @Expose
        val src: String? = null,

        @SerializedName("width")
        val width: Int = 0,

        @SerializedName("height")
        val height: Int = 0
) : Parcelable
