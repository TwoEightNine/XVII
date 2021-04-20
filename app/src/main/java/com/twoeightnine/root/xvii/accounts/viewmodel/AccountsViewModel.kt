package com.twoeightnine.root.xvii.accounts.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.twoeightnine.root.xvii.background.longpoll.LongPollStorage
import com.twoeightnine.root.xvii.lg.L
import com.twoeightnine.root.xvii.storage.SessionProvider
import com.twoeightnine.root.xvii.utils.AsyncUtils
import com.twoeightnine.root.xvii.utils.applyCompletableSchedulers
import global.msnthrp.xvii.core.accounts.AccountsUseCase
import global.msnthrp.xvii.core.accounts.model.Account
import global.msnthrp.xvii.data.accounts.DbAccountsDataSource
import global.msnthrp.xvii.data.db.AppDb
import javax.inject.Inject

class AccountsViewModel(
        private val longPollStorage: LongPollStorage,
        private val appDb: AppDb
) : ViewModel() {

    private val accountsUseCase by lazy {
        AccountsUseCase(DbAccountsDataSource(appDb.accountsDao()))
    }

    private val accountsLiveData = MutableLiveData<List<Account>>()
    private val accountSwitchedLiveData = MutableLiveData<Unit>()

    fun getAccounts() = accountsLiveData as LiveData<List<Account>>

    fun getAccountSwitched() = accountSwitchedLiveData as LiveData<Unit>

    fun loadAccounts() {
        AsyncUtils.onIoThread(accountsUseCase::getAccountToShow, {
            L.tag(TAG).throwable(it).log("loading accounts error")
        }) { accounts ->
            accountsLiveData.value = accounts
            L.tag(TAG).log("loaded")
        }
    }

    fun switchTo(account: Account) {
        AsyncUtils.onIoThread({
            with(account) {
                SessionProvider.token = token
                SessionProvider.userId = userId
                SessionProvider.fullName = name
                SessionProvider.photo = photo
                longPollStorage.clear()
            }
        }) {
            updateRunningAccount()
        }
        appDb.dialogsDao().removeAll()
                .compose(applyCompletableSchedulers())
                .subscribe()
    }

    fun deleteAccount(account: Account) {
        AsyncUtils.onIoThread({ accountsUseCase.deleteAccount(account) }, {
            L.tag(TAG).throwable(it).log("deleting error")
        }) {
            L.tag(TAG).log("removed")
            val accounts = ArrayList(accountsLiveData.value ?: return@onIoThread)
            accounts.remove(account)
            accountsLiveData.value = accounts.toList()
        }
    }

    fun logOutAll() {
        SessionProvider.clearAll()
        appDb.clearAsync()
    }

    private fun updateRunningAccount() {
        AsyncUtils.onIoThread(accountsUseCase::deactivateCurrentAccount, {
            L.tag(TAG).throwable(it).log("error deactivating running account")
        }) {
            accountSwitchedLiveData.value = Unit
        }
    }

    companion object {
        private const val TAG = "accounts"
    }

    class Factory @Inject constructor(
            private val longPollStorage: LongPollStorage,
            private val appDb: AppDb
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AccountsViewModel::class.java)) {
                return AccountsViewModel(longPollStorage, appDb) as T
            }
            throw IllegalArgumentException("Unknown class $modelClass")
        }
    }
}