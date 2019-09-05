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
        val photo200: String? = null
) : Parcelable, ChatOwner {

    override fun getPeerId() = -id

    override fun getAvatar() = photo200

    override fun getTitle() = name

    override fun getInfoText(context: Context): String = context.getString(R.string.community)

    override fun getPrivacyInfo(context: Context): String?  = null
}
