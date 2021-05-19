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

package global.msnthrp.xvii.data.scheduled

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "scheduled_messages")
data class ScheduledMessage(

        @PrimaryKey(autoGenerate = true)
        val id: Int = 0,

        @ColumnInfo(name = "peer_id")
        val peerId: Int = 0,

        @ColumnInfo(name = "when_ms")
        val whenMs: Long = 0L,

        val text: String = "",

        val attachments: String? = null,

        @ColumnInfo(name = "fwd_messages")
        val forwardedMessages: String? = null
)