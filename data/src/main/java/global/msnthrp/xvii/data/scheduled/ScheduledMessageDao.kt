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

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface ScheduledMessageDao {

    @Query("SELECT * FROM scheduled_messages WHERE when_ms > :actualFrom ORDER BY when_ms")
    fun getActualScheduledMessages(actualFrom: Long = System.currentTimeMillis()):
            Single<List<ScheduledMessage>>

    @Query("SELECT * FROM scheduled_messages ORDER BY id DESC LIMIT 1")
    fun getLastScheduledMessage(): Single<List<ScheduledMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addScheduledMessage(scheduledMessage: ScheduledMessage): Completable

    @Delete
    fun deleteScheduledMessage(scheduledMessage: ScheduledMessage): Single<Int>
}