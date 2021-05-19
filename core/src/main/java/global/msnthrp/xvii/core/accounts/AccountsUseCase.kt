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

    fun deleteCurrentAccount() {
        dataSource.apply {
            deleteAccount(getActiveAccount())
        }
    }

    fun deactivateCurrentAccount() {
        val activeAccount = dataSource.getActiveAccount()
                .copy(isActive = false)
        dataSource.updateAccount(activeAccount)
    }

}