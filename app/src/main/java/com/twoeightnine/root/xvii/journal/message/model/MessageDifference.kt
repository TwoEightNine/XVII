package com.twoeightnine.root.xvii.journal.message.model

import android.os.Parcelable
import global.msnthrp.xvii.core.utils.MyersDiff
import kotlinx.android.parcel.Parcelize

object MessageDifference {

    fun from(difference: List<MyersDiff.Change<String>>): List<Change> {
        return difference.map {
            Change(it.elem, when (it) {
                is MyersDiff.Change.Keep -> ChangeType.KEEP
                is MyersDiff.Change.Insert -> ChangeType.INSERT
                is MyersDiff.Change.Remove -> ChangeType.REMOVE
            })
        }
    }
}

@Parcelize
data class Change(
        val word: String,
        val type: ChangeType
) : Parcelable

enum class ChangeType {
    KEEP,
    INSERT,
    REMOVE
}