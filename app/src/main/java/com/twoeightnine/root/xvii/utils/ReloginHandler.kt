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

package com.twoeightnine.root.xvii.utils

import android.annotation.SuppressLint
import android.os.Handler
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.lg.L
import com.twoeightnine.root.xvii.storage.SessionProvider
import global.msnthrp.xvii.core.accounts.AccountsUseCase
import global.msnthrp.xvii.data.accounts.DbAccountsDataSource
import global.msnthrp.xvii.data.db.AppDb
import javax.inject.Inject

class ReloginHandler {

    private var handled = false

    @Inject
    lateinit var appDb: AppDb

    @SuppressLint("CheckResult")
    fun onAuthFailed() {
        if (handled) return

        handled = true
        App.appComponent?.inject(this)
        val accountsUseCase = AccountsUseCase(DbAccountsDataSource(appDb.accountsDao()))
        L.tag(TAG).log("delete account")
        AsyncUtils.onIoThread(accountsUseCase::deleteCurrentAccount, ::onError, ::onSuccess)
    }

    private fun onError(throwable: Throwable) {
        L.tag(TAG).throwable(throwable)
        restart()
    }

    private fun onSuccess(stub: Unit) {
        L.tag(TAG).log("account deleted")
        restart()
    }

    private fun restart() {
        SessionProvider.clearAll()
        Handler().postDelayed({ restartApp(App.context) }, 400L)
    }

    companion object {
        private const val TAG = "relogin"
    }

}