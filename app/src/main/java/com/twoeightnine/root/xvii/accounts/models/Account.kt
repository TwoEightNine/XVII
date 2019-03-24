package com.twoeightnine.root.xvii.accounts.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "accounts")
data class Account(
        @PrimaryKey
        val uid: Int = 0,
        val token: String? = null,
        val name: String? = null,
        val photo: String? = null,
        var isRunning: Boolean = false
) : Parcelable