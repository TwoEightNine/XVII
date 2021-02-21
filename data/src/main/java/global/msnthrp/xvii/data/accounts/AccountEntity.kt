package global.msnthrp.xvii.data.accounts

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import global.msnthrp.xvii.core.accounts.model.Account
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "accounts")
data class AccountEntity(
        @PrimaryKey
        val uid: Int = 0,
        val token: String? = null,
        val name: String? = null,
        val photo: String? = null,
        var isRunning: Boolean = false
) : Parcelable {

    fun toAccount() = Account(
            userId = uid,
            token = token ?: "",
            name = name ?: "",
            photo = photo ?: "",
            isActive = isRunning
    )

    companion object {
        fun fromAccount(account: Account): AccountEntity =
                AccountEntity(
                        uid = account.userId,
                        token = account.token,
                        name = account.name,
                        photo = account.photo,
                        isRunning = account.isActive
                )
    }
}