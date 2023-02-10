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

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.twoeightnine.root.xvii.model.attachments.Attachment
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 * Created by root on 12/31/16.
 */

@Parcelize
data class WallPost(

        @SerializedName("id")
        @Expose
        val id: Int = 0,

        @SerializedName("from_id")
        @Expose
        val fromId: Int = 0,

        @SerializedName("post_type")
        @Expose
        val postType: String? = null,

        @SerializedName("post_id")
        @Expose
        val postId: Int = 0,

        @SerializedName("source_id")
        @Expose
        val sourceId: Int = 0,

        @SerializedName("owner_id")
        @Expose
        val ownerId: Int = 0,

        @SerializedName("date")
        @Expose
        val date: Int = 0,

        @SerializedName("type")
        @Expose
        val type: String? = null,

        @SerializedName("text")
        @Expose
        val text: String? = null,

        @SerializedName("attachments")
        @Expose
        val attachments: ArrayList<Attachment>? = null,

        @SerializedName("likes")
        @Expose
        val likes: Likes? = null,

        @SerializedName("views")
        @Expose
        val views: Views? = null,

        @SerializedName("copy_history")
        val copyHistory: ArrayList<WallPost>? = null,

        // manually added for better ui representativity
        var group: Group? = null,

        var user: User? = null
) : Parcelable {

    val stringId: String
        get() = "${ownerId}_$id"

    fun getPhoto() = attachments?.mapNotNull { it.photo } ?: arrayListOf()

}

@Parcelize
data class Likes(

        @SerializedName("count")
        @Expose
        val count: Int = 0,

        @SerializedName("user_likes")
        @Expose
        var userLikes: Int = 0
) : Parcelable {

    var isUserLiked: Boolean
        get() = userLikes == 1
        set(value) {
            userLikes = if (value) 1 else 0
        }
}

@Parcelize
data class Views(

        @SerializedName("count")
        @Expose
        val count: Int = 0
) : Parcelable
