package com.twoeightnine.root.xvii.model

import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * Created by root on 12/31/16.
 */

class WallPost {

    val id: Int = 0
    @SerializedName("from_id")
    val fromId: Int = 0
    @SerializedName("post_type")
    val postType: String? = null

    @SerializedName("post_id")
    var postId: Int = 0
    @SerializedName("source_id")
    val sourceId: Int = 0
    @SerializedName("owner_id")
    val ownerId: Int = 0
    val date: Int = 0
    val type: String? = null
    val text: String? = null
    val attachments: ArrayList<Attachment>? = null
    val likes: Likes? = null
    val views: Views? = null
    @SerializedName("copy_history")
    val copyHistory: ArrayList<WallPost>? = null

    var group: Group? = null

    var profile: User? = null

    constructor() {
        postId = 0
    }

    constructor(id: Int) {
        postId = id
    }

    val stringId: String
        get() = "${fromId}_$id"

    val photoAttachments: MutableList<Photo>
        get() {
            val photos: MutableList<Photo> = mutableListOf()
            if (attachments == null) {
                return photos
            }
            attachments
                    .filter { it.type == Attachment.TYPE_PHOTO }
                    .all { photos.add(it.photo!!) }
            return photos
        }

    fun owner() = if (sourceId != 0) sourceId else ownerId

    fun item() = if (postId != 0) postId else id

    inner class Likes {
        var count: Int = 0
        @SerializedName("user_likes")
        var userLikes: Int = 0
    }

    inner class Views {
        var count: Int = 0
    }

    companion object {

        var stubLoad = WallPost()

        var stubTry = WallPost()

        private val LOAD = -1
        private val TRY = -2

        fun isStubLoad(wp: WallPost) = wp.postId == LOAD


        fun isStubTry(wp: WallPost) = wp.postId == TRY

    }
}
