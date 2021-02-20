package com.twoeightnine.root.xvii.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.lg.L
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.storage.SessionProvider
import com.twoeightnine.root.xvii.utils.applyCompletableSchedulers
import com.twoeightnine.root.xvii.utils.subscribeSmart
import global.msnthrp.xvii.data.accounts.Account
import global.msnthrp.xvii.data.db.AppDb
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
                user.id,
                token,
                user.fullName,
                user.photoMax ?: "",
                isRunning
        )
        appDb.accountsDao()
                .insertAccount(account)
                .compose(applyCompletableSchedulers())
                .subscribe({
                    L.tag(TAG).log("account updated")
                    accountUpdatedLiveData.value = Unit
                }, {
                    L.tag(TAG).throwable(it).log("updating account error")
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

    companion object {
        private const val TAG = "login"
    }

    data class AccountCheckResult(
            val success: Boolean,
            val token: String? = null,
            val user: User? = null
    )
}