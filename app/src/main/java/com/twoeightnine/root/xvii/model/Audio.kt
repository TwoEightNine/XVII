package com.twoeightnine.root.xvii.model

import android.os.Parcel
import android.os.Parcelable

import com.google.gson.annotations.SerializedName

import java.io.Serializable

/**
 * Created by root on 9/27/16.
 */

class Audio : Parcelable, Serializable {

    @SerializedName("id")
    var id = 0
    @SerializedName("owner_id")
    var ownerId = 0
    @SerializedName("artist")
    var artist: String? = null
    @SerializedName("title")
    var title: String? = null
    @SerializedName("url")
    var url: String? = null
        get() {
            try {
                return field?.substring(0, field?.indexOf("?extra") ?: 0) ?: ""
            } catch (e: Exception) {
                return field ?: ""
            }
        }

    private var duration = 0


    constructor(p: Parcel) {
        id = p.readInt()
        ownerId = p.readInt()
        artist = p.readString()
        title = p.readString()
        url = p.readString()
    }

    constructor(doc: Doc, title: String) {
        val msg = doc.preview?.audioMsg
        url = msg?.linkMp3
        duration = msg?.duration ?: 0
        this.title = title
        this.artist = title
    }

    override fun describeContents() = 0

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeInt(id)
        parcel.writeInt(ownerId)
        parcel.writeString(artist)
        parcel.writeString(title)
        parcel.writeString(url)

    }

    companion object {

        val CREATOR: Parcelable.Creator<Audio> = object : Parcelable.Creator<Audio> {
            override fun createFromParcel(parcel: Parcel): Audio {
                return Audio(parcel)
            }

            override fun newArray(i: Int): Array<Audio?> {
                return arrayOfNulls(i)
            }
        }
    }
}
