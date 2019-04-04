package com.twoeightnine.root.xvii.model

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
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

        var group: Group? = null,

        var profile: User? = null
) : Parcelable {

    val stringId: String
        get() = "${fromId}_$id"

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
