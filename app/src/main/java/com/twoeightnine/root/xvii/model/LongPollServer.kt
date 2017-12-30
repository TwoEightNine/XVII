package com.twoeightnine.root.xvii.model

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

/**
 * Created by root on 9/6/16.
 */

class LongPollServer : Parcelable, Serializable {

    var key: String? = null
    var server: String? = null
    var ts: Int = 0

    constructor(p: Parcel) {
        key = p.readString()
        server = p.readString()
        ts = p.readInt()
    }

    constructor(k: String, s: String, t: Int) {
        ts = t
        key = k
        server = s
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeString(key)
        parcel.writeString(server)
        parcel.writeInt(ts)
    }

    companion object {

        var CREATOR: Parcelable.Creator<LongPollServer> = object : Parcelable.Creator<LongPollServer> {
            override fun createFromParcel(parcel: Parcel): LongPollServer {
                return LongPollServer(parcel)
            }

            override fun newArray(i: Int): Array<LongPollServer?> {
                return arrayOfNulls(i)
            }
        }
    }
}
