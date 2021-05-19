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

import global.msnthrp.xvii.core.accounts.AccountsDataSource
import global.msnthrp.xvii.core.accounts.model.Account
import global.msnthrp.xvii.core.crypto.CryptoUtils
import global.msnthrp.xvii.core.crypto.algorithm.Cipher
import global.msnthrp.xvii.core.utils.toByteArray
import global.msnthrp.xvii.core.utils.toInt
import global.msnthrp.xvii.data.session.EncryptedSessionProvider
import global.msnthrp.xvii.data.utils.ContextHolder

class DbAccountsDataSource(private val accountsDao: AccountsDao) : AccountsDataSource {

    private val key by lazy {
        val context = ContextHolder.contextProvider.applicationContext
        EncryptedSessionProvider(context).encryptionKey256
    }

    override fun getAccounts(activeFirst: Boolean): List<Account> {
        return when {
            activeFirst -> accountsDao.getAccountsRunningFirst()
            else -> accountsDao.getAccounts()
        }.map { it.toAccount() }
    }

    override fun deleteAccount(account: Account) {
        accountsDao.deleteAccount(account.toAccountEntity())
    }

    override fun updateAccount(account: Account) {
        accountsDao.insertAccount(account.toAccountEntity())
    }

    override fun getActiveAccount(): Account = accountsDao.getRunningAccount().toAccount()

    private fun AccountEntity.toAccount(): Account {
        val userId = uid.decrypt()?.toInt() ?: 0
        val token = token?.decrypt()?.let(::String) ?: ""
        val name = name?.decrypt()?.let(::String) ?: ""
        val photo = photo?.decrypt()?.let(::String)

        return Account(
                userId = userId,
                token = token,
                name = name,
                photo = photo,
                isActive = isRunning
        )
    }

    private fun Account.toAccountEntity(): AccountEntity {
        val uid = userId.toByteArray().encrypt()
        val token = token.toByteArray().encrypt()
        val name = name.toByteArray().encrypt()
        val photo = photo?.toByteArray()?.encrypt()

        return AccountEntity(
                uid = uid,
                token = token,
                name = name,
                photo = photo,
                isRunning = isActive
        )
    }

    private fun ByteArray.encrypt(): String =
            Cipher.encrypt(this, key, deterministic = true).let(CryptoUtils::bytesToHex)

    private fun String.decrypt(): ByteArray? = Cipher.decrypt(CryptoUtils.hexToBytes(this), key).bytes
}