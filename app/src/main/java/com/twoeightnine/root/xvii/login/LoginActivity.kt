package com.twoeightnine.root.xvii.login

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.activities.BaseActivity
import com.twoeightnine.root.xvii.background.longpoll.LongPollStorage
import com.twoeightnine.root.xvii.db.AppDb
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

    private val viewModel by lazy {
        ViewModelProviders.of(this)[LoginViewModel::class.java]
    }

    private val isNewAccount by lazy {
        intent?.extras?.getBoolean(ARG_NEW_ACCOUNT) == true
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        App.appComponent?.inject(this)

        viewModel.accountCheckResult.observe(this, Observer(::onAccountChecked))
        viewModel.accountUpdated.observe(this, Observer(::onAccountUpdated))

        if (!isNewAccount) {
            startPrimeGenerator(this)
        }

        if (hasToken()) {
            checkTokenAndStart()
        } else {
            logIn()
        }

        progressBar.stylize()
        webView.setTopInsetMargin()
    }

    override fun getStatusBarColor() = ContextCompat.getColor(this, R.color.splash_background)

    override fun getNavigationBarColor() = ContextCompat.getColor(this, R.color.splash_background)

    override fun styleScreen(container: ViewGroup) {}

    private fun hasToken() = Session.token.isNotBlank()

    private fun checkTokenAndStart() {
        if (isOnline()) {
            viewModel.checkAccount(Session.token, Session.uid, updateSession = !isNewAccount)
        } else {
            startApp()
        }
    }

    private fun logIn() {
        if (isOnline()) {
            showWebView()
        } else {
            finishWithAlert("no internet")
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun showWebView() {
        with(webView) {
            hide()
            CookieSyncManager.createInstance(this@LoginActivity).sync()
            CookieManager.getInstance()
                    .removeAllCookie()
            settings.javaScriptEnabled = true
            settings.javaScriptCanOpenWindowsAutomatically = true
            webViewClient = ParsingWebClient { token, userId ->
                viewModel.checkAccount(token, userId, updateSession = !isNewAccount)
            }

            loadUrl(LOGIN_URL)
            show()
        }
    }

    private fun startApp() {
        longPollStorage.clear()
        MainActivity.launch(this)
        startNotificationService(this)
        this.finish()
    }

    private fun finishWithAlert(text: String) {
        showAlert(this, text) {
            finish()
        }
    }

    private fun onAccountChecked(accountCheckResult: LoginViewModel.AccountCheckResult) {
        when {
            accountCheckResult.success -> viewModel.updateAccount(isRunning = !isNewAccount)
            hasToken() -> showWebView()
            else -> finishWithAlert("unable to log in")
        }
    }

    private fun onAccountUpdated(unit: Unit) {
        if (isNewAccount) {
            finish()
        } else {
            startApp()
        }
    }

//    private fun checkToken() {
//        val token = Session.token
//        val uid = Session.uid
//        if (token.isEmpty()) {
//            toLogIn()
//        } else {
//            apiUtils.checkAccount(token, uid, { toPin() }, { toLogIn() }, { toPin() })
//        }
//    }

//    @SuppressLint("SetJavaScriptEnabled")
//    private fun toLogIn() {
//        webView.hide()
//        webView.settings.javaScriptEnabled = true
//        CookieSyncManager.createInstance(this).sync()
//        val man = CookieManager.getInstance()
//        man.removeAllCookie()
//        webView.settings.javaScriptCanOpenWindowsAutomatically = true
////        webView.webViewClient = ParsingWebClient()
//
//        webView.loadUrl(LOGIN_URL)
//        if (!isOnline()) {
//            showToast(this, R.string.no_internet)
//            finish()
//            return
//        }
//        webView.show()
//    }

//    private fun toPin() {
//        updateAccount()
//        toDialogs()
//    }

//    private fun toDialogs() {
//        MainActivity.launch(this)
//        startNotificationService(this)
//        this.finish()
//    }

//    fun doneWithThis(token: String, userId: Int) {
//        if (token.isBlank() || userId == 0) {
//            onFailed(getString(R.string.invalid_user_id))
//            return
//        }
//        Lg.i("[login] token obtained ...${token.substring(token.length - 6)}")
//
//        rlLoader.show()
//        webView.hide()
//
//        Session.token = token
//        Session.uid = userId
//
//        apiUtils.checkAccount(
//                Session.token,
//                Session.uid,
//                ::onChecked,
//                ::onFailed,
//                ::onLater
//        )
//    }

//    private fun onLater(error: String) {
//        goNext()
//    }

//    private fun onFailed(error: String) {
//        showError(this, error)
//        Session.token = ""
//    }

//    private fun onChecked() {
//        updateAccount()
//        goNext()
//    }

//    @SuppressLint("CheckResult")
//    private fun updateAccount() {
//        val account = Account(
//                Session.uid,
//                Session.token,
//                Session.fullName,
//                Session.photo,
//                true
//        )
//        appDb.accountsDao()
//                .insertAccount(account)
//                .compose(applyCompletableSchedulers())
//                .subscribe({
//                    Lg.i("[login] account updated")
//                }, {
//                    it.printStackTrace()
//                    Lg.wtf("[login] error updating account: ${it.message}")
//                })
//    }

//    private fun goNext() {
//        longPollStorage.clear()
//        MainActivity.launch(this)
//        finish()
//    }

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

        private const val ARG_NEW_ACCOUNT = "newAccount"

        fun launchForNewAccount(context: Context?) {
            context?.startActivity(Intent(context, LoginActivity::class.java).apply {
                putExtra(ARG_NEW_ACCOUNT, true)
            })
        }

    }

    /**
     * handles redirect and calls token parsing
     * @param onLoggedIn callback
     */
    private inner class ParsingWebClient(
            private val onLoggedIn: (String, Int) -> Unit
    ) : WebViewClient() {

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            rlLoader.hide()
            if (url.startsWith(App.REDIRECT_URL)) {
                val token = extract(url, "access_token=(.*?)&")
                val uid = extract(url, "user_id=(\\d*)").toIntOrNull() ?: 0
                onLoggedIn(token, uid)
            }
        }

        private fun extract(from: String, regex: String): String {
            val pattern = Pattern.compile(regex)
            val matcher = pattern.matcher(from)
            if (!matcher.find()) {
                return ""
            }
            return matcher.toMatchResult().group(1)
        }
    }
}