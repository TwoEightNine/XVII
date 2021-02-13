package global.msnthrp.xvii.data.accounts

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface AccountsDao {

    @Query("SELECT * FROM accounts")
    fun getAccounts(): Single<List<Account>>

    @Query("SELECT * FROM accounts WHERE isRunning = 1 LIMIT 1")
    fun getRunningAccount(): Single<Account>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAccount(account: Account): Completable

    @Delete
    fun deleteAccount(account: Account): Single<Int>

    @Query("DELETE FROM accounts WHERE token = :token")
    fun deleteByToken(token: String): Completable

}