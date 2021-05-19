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

package global.msnthrp.xvii.data.accounts

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "accounts")
data class AccountEntity(
        @PrimaryKey
        val uid: String = "",
        val token: String? = null,
        val name: String? = null,
        val photo: String? = null,
        var isRunning: Boolean = false
) : Parcelable {

//    fun toAccount() = Account(
//            userId = uid,
//            token = token ?: "",
//            name = name ?: "",
//            photo = photo ?: "",
//            isActive = isRunning
//    )

    companion object {
//        fun fromAccount(account: Account): AccountEntity =
//                AccountEntity(
//                        uid = account.userId,
//                        token = account.token,
//                        name = account.name,
//                        photo = account.photo,
//                        isRunning = account.isActive
//                )
    }
}