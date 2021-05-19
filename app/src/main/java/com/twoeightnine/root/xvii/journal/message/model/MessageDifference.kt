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