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

package com.twoeightnine.root.xvii.model.attachments

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Poll(

        @SerializedName("id")
        @Expose
        val id: Int,

        @SerializedName("owner_id")
        @Expose
        val ownerId: Int,

        @SerializedName("created")
        @Expose
        val created: Int,

        @SerializedName("question")
        @Expose
        val question: String,

        @SerializedName("votes")
        @Expose
        val votes: Int,

        @SerializedName("answers")
        @Expose
        val answers: List<PollAnswer>,

        @SerializedName("anonymous")
        @Expose
        val anonymous: Boolean,

        @SerializedName("multiple")
        @Expose
        val multiple: Boolean,

        @SerializedName("can_vote")
        @Expose
        val canVote: Boolean,

        @SerializedName("closed")
        @Expose
        val closed: Boolean,

        @SerializedName("answer_ids")
        @Expose
        val answerIds: List<Long> = arrayListOf(),

        @SerializedName("photo")
        val photo: PollPhoto?,

        @SerializedName("background")
        val background: PollBackground?
) : Parcelable, IdTypeable {

    override fun getId() = "poll${ownerId}_$id"
}

@Parcelize
data class PollAnswer(

        @SerializedName("id")
        @Expose
        val id: Long,

        @SerializedName("text")
        @Expose
        val text: String,

        @SerializedName("votes")
        @Expose
        val votes: Int,

        @SerializedName("rate")
        @Expose
        val rate: Double
) : Parcelable

@Parcelize
data class PollPhoto(

        @SerializedName("color")
        val color: String,

        @SerializedName("images")
        val images: List<PollPhotoSize>

) : Parcelable {

    fun getOptimalPhotoUrl(): String? = images.maxByOrNull { it.width }?.url

    fun getColor(): Int = color.asColorInt()
}

@Parcelize
data class PollPhotoSize(

        @SerializedName("url")
        val url: String,

        @SerializedName("width")
        val width: Int,

        @SerializedName("height")
        val height: Int
) : Parcelable

@Parcelize
data class PollBackground(

        @SerializedName("points")
        val points: List<PollBackgroundPoint>
) : Parcelable {

    fun getColors(): Pair<Int, Int> = Pair(points[0].color.asColorInt(), points[1].color.asColorInt())
}

@Parcelize
data class PollBackgroundPoint(

        @SerializedName("color")
        val color: String
) : Parcelable

private fun String.asColorInt(): Int = toInt(16) or 0xff000000.toInt()