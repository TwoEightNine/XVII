package com.twoeightnine.root.xvii.scheduled.core

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