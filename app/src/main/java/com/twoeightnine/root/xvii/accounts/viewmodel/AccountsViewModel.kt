package com.twoeightnine.root.xvii.accounts.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.twoeightnine.root.xvii.background.longpoll.LongPollStorage
import com.twoeightnine.root.xvii.lg.L
import com.twoeightnine.root.xvii.managers.Session
import com.twoeightnine.root.xvii.storage.SessionProvider
import com.twoeightnine.root.xvii.utils.applyCompletableSchedulers
import com.twoeightnine.root.xvii.utils.applySingleSchedulers
import global.msnthrp.xvii.data.accounts.Account
import global.msnthrp.xvii.data.db.AppDb
import javax.inject.Inject

class AccountsViewModel(
        private val longPollStorage: LongPollStorage,
        private val appDb: AppDb
) : ViewModel() {

    private val accountsLiveData = MutableLiveData<ArrayList<Account>>()

    fun getAccounts() = accountsLiveData as LiveData<ArrayList<Account>>

    @SuppressLint("CheckResult")
    fun loadAccounts() {
        appDb.accountsDao().getAccounts()
                .compose(applySingleSchedulers())
                .subscribe({ accounts ->
                    accountsLiveData.value = ArrayList(accounts)
                    L.tag(TAG).log("loaded")
                }, {
                    L.tag(TAG)
                            .warn()
                            .throwable(it)
                            .log("loading accounts error")
                })
    }

    fun switchTo(account: Account) {
        with(account) {
            SessionProvider.token = token
            SessionProvider.userId = uid
            SessionProvider.fullName = name
            SessionProvider.photo = photo
            longPollStorage.clear()
        }
        updateRunningAccount()
        appDb.dialogsDao().removeAll()
                .compose(applyCompletableSchedulers())
                .subscribe()
    }

    @SuppressLint("CheckResult")
    fun deleteAccount(account: Account) {
        appDb.accountsDao().deleteAccount(account)
                .compose(applySingleSchedulers())
                .subscribe({
                    L.tag(TAG).log("removed")
                    val accounts = accountsLiveData.value ?: return@subscribe
                    accounts.remove(account)
                    accountsLiveData.value = accounts
                }, {
                    L.tag(TAG)
                            .warn()
                            .throwable(it)
                            .log("deleting error")
                })
    }

    fun logOut() {
        Session.clearAll()
        appDb.clearAsync()
    }

    // TODO replace with one update query
    @SuppressLint("CheckResult")
    fun updateRunningAccount() {
        appDb.accountsDao().getRunningAccount()
                .compose(applySingleSchedulers())
                .subscribe({ account ->
                    account.isRunning = false
                    appDb.accountsDao().insertAccount(account)
                            .compose(applyCompletableSchedulers())
                            .subscribe()
                }, {
                    L.tag(TAG)
                            .warn()
                            .throwable(it)
                            .log("error getting running")
                })
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