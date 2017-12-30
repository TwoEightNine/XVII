package com.twoeightnine.root.xvii.model

import com.google.gson.annotations.SerializedName
import com.twoeightnine.root.xvii.utils.crypto.CryptoUtil
import java.io.Serializable
import java.util.*

/**
 * Created by root on 10/14/16.
 */

class Doc: Serializable {
    val id: Int = 0
    @SerializedName("owner_id")
    val ownerId: Int = 0
    val title: String? = null
    @SerializedName("access_key")
    val accessKey: String? = null
    val ext: String? = null
    val url: String? = null
    val size: Int = 0
    val type: Int = 0

    //for voice messages and gifs
    val preview: Preview? = null

    val isGif: Boolean
        get() = type == 3 &&
                ext == "gif"

    val isVoiceMessage: Boolean
        get() = type == 5 &&
                preview != null &&
                preview.audioMsg != null

    val isEncrypted: Boolean
        get() = ".$ext" == CryptoUtil.EXTENSION

    override fun toString() = "{id: $id, title: $title, ext: $ext, type: $type, size: $size, url: $url}"

    fun getPreview() = preview?.photo?.sizes?.get(0)?.src ?: ""

    inner class Preview {

        @SerializedName("audio_msg")
        val audioMsg: AudioMsg? = null
        @SerializedName("photo")
        val photo: PhotoPreview? = null

        inner class AudioMsg {

            val duration: Int = 0
            val waveform: ArrayList<Int>? = null
            @SerializedName("link_mp3")
            val linkMp3: String? = null
        }

        inner class PhotoPreview {

            val sizes = mutableListOf<PreviewSize>()

            inner class PreviewSize {

                val src: String? = null

            }

        }
    }
}
