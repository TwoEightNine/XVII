package global.msnthrp.xvii.data.accounts

import androidx.room.*
import io.reactivex.Completable

@Dao
interface AccountsDao {

    @Query("SELECT * FROM accounts")
    fun getAccounts(): List<AccountEntity>

    @Query("SELECT * FROM accounts ORDER BY isRunning DESC")
    fun getAccountsRunningFirst(): List<AccountEntity>

    @Query("SELECT * FROM accounts WHERE isRunning = 1 LIMIT 1")
    fun getRunningAccount(): AccountEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAccount(account: AccountEntity)

    @Delete
    fun deleteAccount(account: AccountEntity): Int

    @Query("DELETE FROM accounts WHERE token = :token")
    fun deleteByToken(token: String): Completable

}