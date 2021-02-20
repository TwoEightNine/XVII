package com.twoeightnine.root.xvii.utils

import android.annotation.SuppressLint
import android.os.Handler
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.lg.L
import com.twoeightnine.root.xvii.managers.Session
import com.twoeightnine.root.xvii.storage.SessionProvider
import global.msnthrp.xvii.data.db.AppDb
import javax.inject.Inject

class ReloginHandler {

    private var handled = false

    @Inject
    lateinit var appDb: AppDb

    @SuppressLint("CheckResult")
    fun onAuthFailed() {
        if (handled) return
        val token = SessionProvider.token ?: return

        handled = true
        App.appComponent?.inject(this)
        L.tag(TAG).log("delete account")
        appDb.accountsDao().deleteByToken(token)
                .compose(applyCompletableSchedulers())
                .subscribe({
                    L.tag(TAG).log("account deleted")
                    restart()
                }, { throwable ->
                    L.tag(TAG).throwable(throwable)
                    restart()
                })
    }

    private fun restart() {
        Session.clearAll()
        Handler().postDelayed({ restartApp(App.context) }, 400L)
    }

    companion object {
        private const val TAG = "relogin"
    }

}