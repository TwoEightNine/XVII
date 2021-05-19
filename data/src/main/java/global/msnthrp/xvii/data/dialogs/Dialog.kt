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

package global.msnthrp.xvii.data.dialogs

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "dialogs")
data class Dialog(
        @PrimaryKey
        val peerId: Int = 0,
        var messageId: Int = 0,
        val title: String = "",
        val photo: String? = null,
        var text: String = "",
        var timeStamp: Int = 0,
        var isOut: Boolean = false,
        var isRead: Boolean = true,
        var unreadCount: Int = 0,
        var isOnline: Boolean = false,
        var isMute: Boolean = false,
        var isPinned: Boolean = false,
        var alias: String? = null
) : Parcelable {

    val aliasOrTitle: String
        get() = alias ?: title

}