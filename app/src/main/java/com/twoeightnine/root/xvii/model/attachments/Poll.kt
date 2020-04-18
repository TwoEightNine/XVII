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
        val answerIds: List<Int> = arrayListOf(),

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
        val id: Int,

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

    fun getOptimalPhotoUrl(): String? = images.maxBy { it.width }?.url

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