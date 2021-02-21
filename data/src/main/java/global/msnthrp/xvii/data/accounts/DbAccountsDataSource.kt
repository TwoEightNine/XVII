package global.msnthrp.xvii.data.accounts

import global.msnthrp.xvii.core.accounts.AccountsDataSource
import global.msnthrp.xvii.core.accounts.model.Account

class DbAccountsDataSource(private val accountsDao: AccountsDao) : AccountsDataSource {

    override fun getAccounts(activeFirst: Boolean): List<Account> {
        return when {
            activeFirst -> accountsDao.getAccountsRunningFirst()
            else -> accountsDao.getAccounts()
        }.map { it.toAccount() }
    }

    override fun deleteAccount(account: Account) {
        accountsDao.deleteAccount(AccountEntity.fromAccount(account))
    }

    override fun updateAccount(account: Account) {
        accountsDao.insertAccount(AccountEntity.fromAccount(account))
    }

    override fun getActiveAccount(): Account = accountsDao.getRunningAccount().toAccount()
}