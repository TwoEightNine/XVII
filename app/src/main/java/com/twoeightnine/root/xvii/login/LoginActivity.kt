package com.twoeightnine.root.xvii.login

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
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
import com.twoeightnine.root.xvii.background.longpoll.LongPollStorage
import com.twoeightnine.root.xvii.base.BaseActivity
import com.twoeightnine.root.xvii.lg.L
import com.twoeightnine.root.xvii.main.MainActivity
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.managers.Session
import com.twoeightnine.root.xvii.pin.SecurityFragment
import com.twoeightnine.root.xvii.pin.fake.alarm.AlarmActivity
import com.twoeightnine.root.xvii.utils.*
import kotlinx.android.synthetic.main.activity_login.*
import java.util.regex.Pattern
import javax.inject.Inject

class LoginActivity : BaseActivity() {

    @Inject
    lateinit var longPollStorage: LongPollStorage

    private val viewModel by lazy {
        ViewModelProviders.of(this)[LoginViewModel::class.java]
    }
    private val addNewAccount by lazy {
        intent?.extras?.getBoolean(ARG_NEW_ACCOUNT) == true
    }

    private val fakeAppTouchListener by lazy {
        FakeAppTouchListener()
    }

    private var isWebViewShown = false

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        rlLoader.setOnTouchListener(fakeAppTouchListener)
        App.appComponent?.inject(this)

        viewModel.accountCheckResult.observe(this, Observer(::onAccountChecked))
        viewModel.accountUpdated.observe(this, Observer(::onAccountUpdated))

        if (!addNewAccount) {
            startPrimeGenerator(this)
        }
        if (addNewAccount || !hasToken()) {
            logIn()
        } else {
            checkTokenAndStart()
        }

        progressBar.stylize()
        webView.setTopInsetMargin()
    }

    override fun getStatusBarColor() = if (isWebViewShown) {
        Color.WHITE
    } else {
        ContextCompat.getColor(this, R.color.splash_background)
    }

    override fun getNavigationBarColor() = if (isWebViewShown) {
        Color.WHITE
    } else{
        ContextCompat.getColor(this, R.color.splash_background)
    }

    override fun styleScreen(container: ViewGroup) {}

    private fun hasToken() = Session.token.isNotBlank()

    private fun checkTokenAndStart() {
        if (isOnline()) {
            viewModel.checkAccount(Session.token, Session.uid)
        } else {
            startApp()
        }
    }

    private fun logIn() {
        if (isOnline()) {
            showWebView()
        } else {
            finishWithAlert(getString(R.string.login_no_internet))
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
                viewModel.checkAccount(token, userId)
            }

            loadUrl(LOGIN_URL)
            show()
            isWebViewShown = true
        }
    }

    private fun startApp() {
        longPollStorage.clear()
        startNotificationService(this)
        when (Prefs.fakeAppType) {
            SecurityFragment.FakeAppType.ALARMS ->
                AlarmActivity.launch(this)

            SecurityFragment.FakeAppType.DIAGNOSTICS -> {}

            SecurityFragment.FakeAppType.NONE ->
                MainActivity.launch(this)
        }
        this.finish()
    }

    private fun finishWithAlert(text: String) {
        showAlert(this, text) {
            finish()
        }
    }

    private fun onAccountChecked(accountCheckResult: LoginViewModel.AccountCheckResult) {
        val user = accountCheckResult.user
        val token = accountCheckResult.token
        when {
            accountCheckResult.success && user != null && token != null -> {
                if (!addNewAccount) {
                    Session.token = token
                    Session.uid = user.id
                    Session.fullName = user.fullName
                    Session.photo = user.photo100 ?: ""
                }
                viewModel.updateAccount(user, token, isRunning = !addNewAccount)
            }
            hasToken() -> showWebView()
            else -> finishWithAlert(getString(R.string.login_unable_to_log_in))
        }
    }

    private fun onAccountUpdated(unit: Unit) {
        if (addNewAccount) {
            finish()
        } else {
            startApp()
        }
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
                view.hide()
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

    private inner class FakeAppTouchListener : View.OnTouchListener {

        var passed = false
            private set

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            if (event?.action == MotionEvent.ACTION_DOWN) {
                passed = true
                L.tag("fake app").log("passed")
            }
            return true
        }
    }
}