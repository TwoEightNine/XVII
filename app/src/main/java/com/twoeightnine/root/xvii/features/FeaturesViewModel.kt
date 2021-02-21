package com.twoeightnine.root.xvii.features

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.lg.L
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.storage.SessionProvider
import com.twoeightnine.root.xvii.utils.AsyncUtils
import com.twoeightnine.root.xvii.utils.subscribeSmart
import global.msnthrp.xvii.core.accounts.AccountsUseCase
import global.msnthrp.xvii.core.accounts.model.Account
import global.msnthrp.xvii.data.accounts.DbAccountsDataSource
import global.msnthrp.xvii.data.db.AppDb
import javax.inject.Inject

class FeaturesViewModel(
        private val appDb: AppDb,
        private val api: ApiService
) : ViewModel() {

    private val accountsUseCase by lazy {
        AccountsUseCase(DbAccountsDataSource(appDb.accountsDao()))
    }

    private val accountLiveData = MutableLiveData<Account>()
    private val lastSeenLiveData = MutableLiveData<Pair<Boolean, Int>>()

    val lastSeen: LiveData<Pair<Boolean, Int>>
        get() = lastSeenLiveData

    fun getAccount() = accountLiveData as LiveData<Account>

    @SuppressLint("CheckResult")
    fun loadAccount() {
        AsyncUtils.onIoThread(accountsUseCase::getActiveAccount, {
            L.tag(TAG).throwable(it).log("error loading account")
        }) { account ->
            accountLiveData.value = account
        }
    }

    fun shareXvii(onSuccess: () -> Unit, onError: (String) -> Unit) {
        api.repost(App.SHARE_POST)
                .subscribeSmart({
                    onSuccess()
                }, onError)
    }

    fun checkMembership(callback: (Boolean) -> Unit) {
        api.isGroupMember(App.GROUP, SessionProvider.userId)
                .subscribeSmart({
                    callback.invoke(it == 1)
                }, { error ->
                    L.tag(TAG)
                            .warn()
                            .log("check membership error: $error")
                })
    }

    fun joinGroup() {
        api.joinGroup(App.GROUP)
                .subscribeSmart({}, {})
    }

    fun updateLastSeen() {
        api.getUsers("${SessionProvider.userId}", "online,last_seen")
                .subscribeSmart({ users ->
                    users.getOrNull(0)?.also { user ->
                        lastSeenLiveData.value = Pair(user.isOnline, user.lastSeen?.time ?: 0)
                    }
                }, {})
    }

    companion object {
        private const val TAG = "features"
    }

    class Factory @Inject constructor(
            private val appDb: AppDb,
            private val api: ApiService
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass == FeaturesViewModel::class.java) {
                return FeaturesViewModel(appDb, api) as T
            }
            throw IllegalArgumentException("Unknown ViewModel $modelClass")
        }
    }

}