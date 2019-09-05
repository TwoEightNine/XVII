package com.twoeightnine.root.xvii.model

import android.content.Context
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chatowner.model.ChatOwner
import kotlinx.android.parcel.Parcelize

/**
 * Created by root on 10/23/16.
 */

@Parcelize
data class Group(

        @SerializedName("id")
        @Expose
        val id: Int = 0,

        @SerializedName("name")
        @Expose
        val name: String = "",

        @SerializedName("photo_50")
        @Expose
        val photo50: String? = null,

        @SerializedName("photo_100")
        @Expose
        val photo100: String = "http://www.iconsdb.com/icons/preview/light-gray/square-xxl.png",

        @SerializedName("photo_200")
        @Expose
        val photo200: String? = null,

        @SerializedName("is_closed")
        val isClosed: Int = 0,

        @SerializedName("screen_name")
        val screenName: String = "",

        @SerializedName("description")
        val description: String = "",

        @SerializedName("status")
        val status: String = "",

        @SerializedName("members_count")
        val membersCount: Int = 0
) : Parcelable, ChatOwner {

    override fun getPeerId() = -id

    override fun getAvatar() = photo200

    override fun getTitle() = name

    override fun getInfoText(context: Context): String =
            context.resources.getQuantityString(R.plurals.participants, membersCount, membersCount)

    override fun getPrivacyInfo(context: Context): String? = when {
        isClosed != 0 -> context.getString(R.string.group_closed)
        else -> null
    }

    companion object {
        const val FIELDS = "place,description,members_count,status"
    }
}
