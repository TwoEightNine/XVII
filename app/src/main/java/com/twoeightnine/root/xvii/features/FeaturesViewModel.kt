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
    private val lastSeenLiveData = MutableLiveData<Triple<Boolean, Int, Int>>()

    val lastSeen: LiveData<Triple<Boolean, Int, Int>>
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
                        lastSeenLiveData.value = Triple(
                                user.isOnline,
                                user.lastSeen?.time ?: 0,
                                user.lastSeen?.platform ?: 0
                        )
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