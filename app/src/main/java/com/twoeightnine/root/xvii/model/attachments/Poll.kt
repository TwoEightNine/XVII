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

        @SerializedName("answer_ids")
        @Expose
        val answerIds: List<Int> = arrayListOf()
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