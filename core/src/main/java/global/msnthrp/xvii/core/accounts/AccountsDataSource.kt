package global.msnthrp.xvii.core.accounts

import global.msnthrp.xvii.core.accounts.model.Account

interface AccountsDataSource {

    fun getAccounts(activeFirst: Boolean = false): List<Account>

    fun getActiveAccount(): Account

    fun deleteAccount(account: Account)

    fun updateAccount(account: Account)
}