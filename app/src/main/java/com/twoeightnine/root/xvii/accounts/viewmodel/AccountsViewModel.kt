package com.twoeightnine.root.xvii.accounts.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.twoeightnine.root.xvii.accounts.models.Account
import com.twoeightnine.root.xvii.background.longpoll.LongPollStorage
import com.twoeightnine.root.xvii.db.AppDb
import com.twoeightnine.root.xvii.lg.Lg
import com.twoeightnine.root.xvii.managers.Session
import com.twoeightnine.root.xvii.utils.applyCompletableSchedulers
import com.twoeightnine.root.xvii.utils.applySingleSchedulers
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
                    l("loaded")
                    if (accounts.isEmpty()) {
                        restoreFromSession()
                    }
                }, {
                    it.printStackTrace()
                    lw("loading error: ${it.message}")
                })
    }

    fun switchTo(account: Account) {
        with(account) {
            Session.token = token
            Session.uid = uid
            Session.fullName = name
            Session.photo = photo
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
                    l("removed")
                    val accounts = accountsLiveData.value ?: return@subscribe
                    accounts.remove(account)
                    accountsLiveData.value = accounts
                }, {
                    it.printStackTrace()
                    lw("deleting error: ${it.message}")
                })
    }

    fun logOut() {
        Session.token = ""
        appDb.clearAsync()
//        CacheHelper.deleteAllMessagesAsync()
    }

    /**
     * in case of first launching with this new database
     */
    @SuppressLint("CheckResult")
    private fun restoreFromSession() {
        val account = Account(
                Session.uid,
                Session.token,
                Session.fullName,
                Session.photo,
                true
        )
        appDb.accountsDao().insertAccount(account)
                .compose(applyCompletableSchedulers())
                .subscribe({
                    l("restored from session")
                    accountsLiveData.value = arrayListOf(account)
                }, {
                    it.printStackTrace()
                    lw("restoring error: ${it.message}")
                })
    }

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
                    it.printStackTrace()
                    lw("error getting running: ${it.message}")
                })
    }

    private fun l(s: String) {
        Lg.i("[accounts] $s")
    }

    private fun lw(s: String) {
        Lg.wtf("[accounts] $s")
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