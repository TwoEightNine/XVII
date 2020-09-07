package com.twoeightnine.root.xvii.scheduled.core

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