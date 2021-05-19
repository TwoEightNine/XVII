/*
 * xvii - messenger for vk
 * Copyright (C) 2021  TwoEightNine
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.twoeightnine.root.xvii.model

import android.content.Context
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chatowner.model.ChatOwner
import com.twoeightnine.root.xvii.utils.shortifyNumber
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
            context.resources.getQuantityString(R.plurals.participants, membersCount, shortifyNumber(membersCount))

    override fun getPrivacyInfo(context: Context): String? = when {
        isClosed != 0 -> context.getString(R.string.group_closed)
        else -> null
    }

    companion object {
        const val FIELDS = "place,description,members_count,status"
    }
}
