package com.twoeightnine.root.xvii.model

import android.content.Context
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chatowner.model.ChatOwner
import com.twoeightnine.root.xvii.utils.getLastSeenText
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(

        @SerializedName("id")
        @Expose
        val id: Int = 0,

        @SerializedName("first_name")
        @Expose
        val firstName: String? = null,

        @SerializedName("last_name")
        @Expose
        val lastName: String? = null,

        @SerializedName("deactivated")
        val deactivated: String? = null,

        @SerializedName("hidden")
        val hidden: Int? = null,

        @SerializedName("is_closed")
        @Expose
        val isClosed: Boolean = false,

        @SerializedName("can_access_closed")
        val canAccessClosed: Boolean = false,

        @SerializedName("can_write_private_message")
        val canWrite: Int = 0,

        @SerializedName("blacklisted")
        val blacklisted: Int = 0,

        @SerializedName("photo_max_orig")
        @Expose
        val photoMax: String? = null,

        @SerializedName("photo_50")
        @Expose
        val photo50: String? = null,

        @SerializedName("photo_100")
        @Expose
        val photo100: String? = null,

        @SerializedName("domain")
        @Expose
        val domain: String? = null,

        @SerializedName("status")
        @Expose
        val status: String? = null,

        @SerializedName("city")
        @Expose
        val city: City? = null,

        @SerializedName("home_town")
        @Expose
        val hometown: String? = null,

        @SerializedName("bdate")
        @Expose
        val bdate: String? = null,

        @SerializedName("relation")
        @Expose
        val relation: Int? = null,

        @SerializedName("photo_id")
        @Expose
        val photoId: String? = null,

        @SerializedName("online")
        @Expose
        var online: Int? = null,

        @SerializedName("last_seen")
        @Expose
        val lastSeen: LastSeen? = null,

        @SerializedName("counters")
        @Expose
        val counters: Counters? = null,

        @SerializedName("mobile_phone")
        @Expose
        val mobilePhone: String? = null,

        @SerializedName("home_phone")
        @Expose
        val homePhone: String? = null,

        @SerializedName("site")
        @Expose
        val site: String? = null,

        @SerializedName("instagram")
        @Expose
        val instagram: String? = null,

        @SerializedName("skype")
        @Expose
        val skype: String? = null,

        @SerializedName("facebook")
        @Expose
        val facebook: String? = null,

        @SerializedName("twitter")
        @Expose
        val twitter: String? = null
) : Parcelable, ChatOwner {

    override fun getPeerId() = id

    override fun getAvatar() = photoMax

    override fun getTitle() = fullName

    override fun getInfoText(context: Context): String =
            getLastSeenText(
                    context.resources, isOnline,
                    lastSeen?.time ?: 0,
                    lastSeen?.platform ?: 0
            )

    override fun getPrivacyInfo(context: Context): String? = when {
        deactivated != null -> context.getString(R.string.profile_deactivated)
        blacklisted == 1 -> context.getString(R.string.profile_blacklisted_you)
        isClosed && !canAccessClosed -> context.getString(R.string.profile_closed)
        else -> null
    }

    companion object {
        const val FIELDS = "photo_max_orig,photo_50,photo_100,domain,city,status," +
                "home_town,bdate,relation,photo_id,online,last_seen,counters,contacts,site," +
                "connections,is_closed,blacklisted,can_write_private_message"

        const val VK = "https://vk.com/"
        const val TWITTER = "https://twitter.com/"
        const val INST = "https://instagram.com/"
    }

    fun getPageName() = domain ?: "id$id"

    val link: String
        get() = "$VK${getPageName()}"

    val linkTwitter: String
        get() = "$TWITTER$twitter"

    val linkInst: String
        get() = "$INST$instagram"

    val fullName: String
        get() = "$firstName $lastName"

    var isOnline: Boolean
        get() = (online ?: 0) == 1
        set(isOnline) {
            online = if (isOnline) 1 else 0
        }

    val canWriteThisUser: Boolean
        get() = blacklisted == 0 && canWrite == 1
}

@Parcelize
data class City(

        @SerializedName("id")
        @Expose
        val id: Int = 0,

        @SerializedName("title")
        @Expose
        val title: String? = null
) : Parcelable

@Parcelize
data class LastSeen(
        @SerializedName("time")
        @Expose
        var time: Int = 0,

        @SerializedName("platform")
        @Expose
        var platform: Int = 0
) : Parcelable

@Parcelize
data class Counters(

        @SerializedName("friends")
        @Expose
        val friends: Int = 0,

        @SerializedName("online_friends")
        @Expose
        val onlineFriends: Int = 0,

        @SerializedName("mutual_friends")
        @Expose
        val mutual: Int = 0,

        @SerializedName("followers")
        @Expose
        val followers: Int = 0
) : Parcelable
