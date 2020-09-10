package com.twoeightnine.root.xvii.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.accounts.models.Account
import com.twoeightnine.root.xvii.db.AppDb
import com.twoeightnine.root.xvii.lg.Lg
import com.twoeightnine.root.xvii.managers.Session
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.utils.applyCompletableSchedulers
import com.twoeightnine.root.xvii.utils.subscribeSmart
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class LoginViewModel : ViewModel() {

    @Inject
    lateinit var api: ApiService

    @Inject
    lateinit var appDb: AppDb

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

    fun checkAccount(token: String, userId: Int, updateSession: Boolean) {
        api.checkUser(userId.toString(), token)
                .subscribeSmart({ response ->
                    val user = response.getOrNull(0)
                    if (user == null) {
                        accountCheckResultLiveData.value = AccountCheckResult(success = false)
                    } else {
                        if (updateSession) {
                            Session.token = token
                            Session.uid = userId
                            Session.fullName = user.fullName
                            Session.photo = user.photo100 ?: "errrr"
                        }
                        accountCheckResultLiveData.value = AccountCheckResult(success = true)
                    }
                }, { error ->
                    Lg.wtf("check acc error: $error")
                    accountCheckResultLiveData.value = AccountCheckResult(success = false)
                })
                .addToDisposables()

    }

    fun updateAccount(isRunning: Boolean) {
        val account = Account(
                Session.uid,
                Session.token,
                Session.fullName,
                Session.photo,
                isRunning
        )
        appDb.accountsDao()
                .insertAccount(account)
                .compose(applyCompletableSchedulers())
                .subscribe({
                    Lg.i("[login] account updated")
                    accountUpdatedLiveData.value = Unit
                }, {
                    it.printStackTrace()
                    Lg.wtf("[login] error updating account: ${it.message}")
                })
                .addToDisposables()
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    private fun Disposable.addToDisposables() {
        compositeDisposable.add(this)
    }

    data class AccountCheckResult(
            val success: Boolean
    )
}