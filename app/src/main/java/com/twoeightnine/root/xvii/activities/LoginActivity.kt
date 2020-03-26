package com.twoeightnine.root.xvii.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.accounts.models.Account
import com.twoeightnine.root.xvii.background.longpoll.LongPollStorage
import com.twoeightnine.root.xvii.db.AppDb
import com.twoeightnine.root.xvii.lg.Lg
import com.twoeightnine.root.xvii.main.MainActivity
import com.twoeightnine.root.xvii.managers.Session
import com.twoeightnine.root.xvii.utils.*
import kotlinx.android.synthetic.main.activity_login.*
import java.util.regex.Pattern
import javax.inject.Inject

class LoginActivity : BaseActivity() {

    @Inject
    lateinit var apiUtils: ApiUtils

    @Inject
    lateinit var longPollStorage: LongPollStorage

    @Inject
    lateinit var appDb: AppDb

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        startPrimeGenerator(this)

        App.appComponent?.inject(this)

        checkToken()
        progressBar.stylize()
    }

    private fun checkToken() {
        val token = Session.token
        val uid = Session.uid
        if (token.isNullOrEmpty()) {
            toLogIn()
        } else {
            apiUtils.checkAccount(token, uid, { toPin() }, { toLogIn() }, { toPin() })
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun toLogIn() {
        webView.hide()
        webView.settings?.javaScriptEnabled = true
        CookieSyncManager.createInstance(this).sync()
        val man = CookieManager.getInstance()
        man.removeAllCookie()
        webView.settings?.javaScriptCanOpenWindowsAutomatically = true
        webView.webViewClient = ParsingWebClient()

        webView.loadUrl(LOGIN_URL)
        if (!isOnline()) {
            showToast(this, R.string.no_internet)
            finish()
            return
        }
        webView.show()
    }

    private fun toPin() {
        updateAccount()
        toDialogs()
    }

    private fun toDialogs() {
//        startActivity(Intent(this, RootActivity::class.java))
        MainActivity.launch(this)
        startNotificationService(this)
        this.finish()
    }

    fun doneWithThis(url: String) {
        val token = extract(url, "access_token=(.*?)&")
        val uid: Int
        try {
            uid = extract(url, "user_id=(\\d*)").toInt()
        } catch (e: Exception) {
            onFailed(getString(R.string.invalid_user_id))
            return
        }
        Lg.i("[login] token obtained ...${token.substring(token.length - 6)}")

        rlLoader.show()
        webView.hide()

        Session.token = token
        Session.uid = uid

        apiUtils.checkAccount(
                Session.token,
                Session.uid,
                ::onChecked,
                ::onFailed,
                ::onLater
        )
    }

    private fun onLater(error: String) {
        goNext()
    }

    private fun onFailed(error: String) {
        showError(this, error)
        Session.token = ""
    }

    private fun onChecked() {
        updateAccount()
        goNext()
    }

    @SuppressLint("CheckResult")
    private fun updateAccount() {
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
                    Lg.i("[login] account updated")
                }, {
                    it.printStackTrace()
                    Lg.wtf("[login] error updating account: ${it.message}")
                })
    }

    private fun extract(from: String, regex: String): String {
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(from)
        if (!matcher.find()) {
            return ""
        }
        return matcher.toMatchResult().group(1)
    }

    private fun goNext() {
        longPollStorage.clear()
//        startActivity(Intent(this, RootActivity::class.java))
        MainActivity.launch(this)
        finish()
    }

    override fun getThemeId() = R.style.SplashTheme

    companion object {

        private const val LOGIN_URL = "https://oauth.vk.com/authorize?" +
                "client_id=${App.APP_ID}&" +
                "scope=${App.SCOPE_ALL}&" +
                "redirect_uri=${App.REDIRECT_URL}&" +
                "display=touch&" +
                "v=${App.VERSION}&" +
                "response_type=token"
        // https://oauth.vk.com/authorize?client_id=6079611&scope=&redirect_uri=https://oauth.vk.com/blank.html&display=touch&v=5.95&response_type=token

    }

    /**
     * handles redirect and calls token parsing
     */
    private inner class ParsingWebClient : WebViewClient() {

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            rlLoader.hide()
            if (url.startsWith(App.REDIRECT_URL)) {
                doneWithThis(url)
            }
        }
    }
}