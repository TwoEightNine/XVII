package global.msnthrp.xvii.core.accounts

import global.msnthrp.xvii.core.accounts.model.Account

class AccountsUseCase(private val dataSource: AccountsDataSource) {

    fun getAccountToShow(): List<Account> = dataSource.getAccounts(activeFirst = true)

    fun getActiveAccount(): Account = dataSource.getActiveAccount()

    fun updateAccount(account: Account) {
        dataSource.updateAccount(account)
    }

    fun deleteAccount(account: Account) {
        if (!account.isActive) {
            dataSource.deleteAccount(account)
        }
    }

    fun deactivateCurrentAccount() {
        val activeAccount = dataSource.getActiveAccount()
                .copy(isActive = false)
        dataSource.updateAccount(activeAccount)
    }

}