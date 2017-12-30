package com.twoeightnine.root.xvii.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.twoeightnine.root.xvii.utils.getTime
import io.realm.annotations.Ignore
import java.io.Serializable

class User : Parcelable, Serializable {

    @SerializedName("id")
    @Expose
    var id: Int = 0

    @SerializedName("first_name")
    @Expose
    var firstName: String? = null

    @SerializedName("last_name")
    @Expose
    var lastName: String? = null

    @SerializedName("deactivated")
    var deactivated: String? = null

    @SerializedName("hidden")
    @Ignore
    var hidden: Int? = null

    @SerializedName("photo_max_orig")
    var photoMax: String? = null
    @SerializedName("photo_50")
    var photo50: String? = null
    @SerializedName("photo_100")
    var photo100: String? = null
    @SerializedName("domain")
    private var domain: String? = null
    @SerializedName("status")
    var status: String? = null
    @SerializedName("city")
    var city: City? = null
    @SerializedName("home_town")
    var hometown: String? = null
    @SerializedName("bdate")
    var bdate: String? = null
    @SerializedName("relation")
    var relation: Int? = null
    @SerializedName("photo_id")
    var photoID: String? = null
    @SerializedName("online")
    var online: Int? = null
    @SerializedName("last_seen")
    var lastSeen: LastSeen? = null
    @SerializedName("counters")
    var counters: Counters? = null
    @SerializedName("mobile_phone")
    var mobilePhone: String? = null
    @SerializedName("home_phone")
    var homePhone: String? = null
    @SerializedName("site")
    var site: String? = null
    @SerializedName("instagram")
    var instagram: String? = null
    @SerializedName("skype")
    var skype: String? = null
    @SerializedName("facebook")
    var facebook: String? = null
    @SerializedName("twitter")
    var twitter: String? = null

    constructor(p: Parcel) {
        id = p.readInt()
        firstName = p.readString()
        lastName = p.readString()
        deactivated = p.readString()
        hidden = p.readInt()

        photoMax = p.readString()
        photo50 = p.readString()
        photo100 = p.readString()
        domain = p.readString()
        status = p.readString()
        hometown = p.readString()
        bdate = p.readString()
        relation = p.readInt()
        photoID = p.readString()
        online = p.readInt()
    }

    constructor(id: Int) {
        this.id = id
    }

    constructor(userDb: UserDb) {
        id = userDb.id
        firstName = userDb.firstName
        lastName = userDb.lastName
        photoMax = userDb.photoMax
        photo100 = photoMax
    }

    override fun describeContents() = id

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeInt(id)
        parcel.writeString(firstName)
        parcel.writeString(lastName)
        parcel.writeString(deactivated)
        parcel.writeInt(hidden ?: 0)

        parcel.writeString(photoMax)
        parcel.writeString(photo50)
        parcel.writeString(photo100)
        parcel.writeString(domain)
        parcel.writeString(status)
        parcel.writeString(hometown)
        parcel.writeString(bdate)
        parcel.writeInt(relation ?: 0)
        parcel.writeString(photoID)
        parcel.writeInt(online ?: 0)
    }

    inner class City {

        var id: Int = 0
        var title: String? = null

    }

    override fun toString() = "$id ${fullName()} $photo100"

    fun fullName() = "$firstName $lastName"

    fun getLink() = VK + getDomain()

    fun getDomain() = domain ?: "id$id"

    inner class LastSeen {
        var time: Int = 0

        override fun toString() = getTime(time, true)
    }

    inner class Counters {
        var friends: Int = 0
        @SerializedName("online_friends")
        var onlineFriends: Int = 0
        var mutual: Int = 0
        var followers: Int = 0
    }

    companion object {

        var stubLoad = User(-1)
        var stubTry = User(-2)

        var VK = "https://vk.com/"
        //---------------------------
        var FIELDS = "photo_max_orig,photo_50,photo_100,domain,city,status," +
                "home_town,bdate,relation,photo_id,online,last_seen,counters,contacts,site,connections"


        fun isStubLoad(user: User) = user.id == -1

        fun isStubTry(user: User) = user.id == -2

        var CREATOR: Parcelable.Creator<User> = object : Parcelable.Creator<User> {
            override fun createFromParcel(parcel: Parcel): User {
                return User(parcel)
            }

            override fun newArray(i: Int): Array<User?> {
                return arrayOfNulls(i)
            }
        }
    }
}