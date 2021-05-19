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

package com.twoeightnine.root.xvii.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.lg.L
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.storage.SessionProvider
import com.twoeightnine.root.xvii.utils.AsyncUtils
import com.twoeightnine.root.xvii.utils.subscribeSmart
import global.msnthrp.xvii.core.accounts.AccountsUseCase
import global.msnthrp.xvii.core.accounts.model.Account
import global.msnthrp.xvii.data.accounts.DbAccountsDataSource
import global.msnthrp.xvii.data.db.AppDb
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class LoginViewModel : ViewModel() {

    @Inject
    lateinit var api: ApiService

    @Inject
    lateinit var appDb: AppDb
    
    private val accountsUseCase by lazy { 
        AccountsUseCase(DbAccountsDataSource(appDb.accountsDao()))
    }

    private val multiCancellables = AsyncUtils.MultiCancellable()
    private val compositeDisposable = CompositeDisposable()

    private val accountUpdatedLiveData = MutableLiveData<Unit>()
    private val accountCheckResultLiveData = MutableLiveData<AccountCheckResult>()

    val accountUpdated: LiveData<Unit>
        get() = accountUpdatedLiveData

    val accountCheckResult: LiveData<AccountCheckResult>
        get() = accountCheckResultLiveData

    init {
        App.appComponent?.inject(this)
    }

    fun checkAccount(token: String?, userId: Int) {
        api.checkUser(userId.toString(), token)
                .subscribeSmart({ response ->
                    val user = response.getOrNull(0)
                    accountCheckResultLiveData.value = if (user == null) {
                        AccountCheckResult(success = false)
                    } else {
                        AccountCheckResult(
                                success = true,
                                user = user,
                                token = token
                        )
                    }
                }, { error ->
                    L.tag(TAG).warn().log("check account error: $error")
                    accountCheckResultLiveData.value = AccountCheckResult(success = false)
                })
                .addToDisposables()

    }

    fun updateAccount(user: User, token: String, isRunning: Boolean) {
        if (isRunning) {
            SessionProvider.token = token
            SessionProvider.userId = user.id
            SessionProvider.fullName = user.fullName
            SessionProvider.photo = user.photoMax
        }
        val account = Account(
                userId = user.id,
                token = token,
                name = user.fullName,
                photo = user.photoMax,
                isActive = isRunning
        )
        AsyncUtils.onIoThread({ accountsUseCase.updateAccount(account) }, {
            L.tag(TAG).throwable(it).log("updating account error")
        }) {
            L.tag(TAG).log("account updated")
            accountUpdatedLiveData.value = Unit
        }.addToMultiCancellable()
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    private fun Disposable.addToDisposables() {
        compositeDisposable.add(this)
    }

    private fun AsyncUtils.Cancellable.addToMultiCancellable() {
        multiCancellables.add(this)
    }

    companion object {
        private const val TAG = "login"
    }

    data class AccountCheckResult(
            val success: Boolean,
            val token: String? = null,
            val user: User? = null
    )
}