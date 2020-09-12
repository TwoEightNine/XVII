package com.twoeightnine.root.xvii.utils

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.lg.L
import com.twoeightnine.root.xvii.login.LoginActivity
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.managers.Session
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.network.response.BaseResponse
import com.twoeightnine.root.xvii.pin.PinActivity
import com.twoeightnine.root.xvii.pin.fake.alarm.AlarmActivity
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AppLifecycleTracker : Application.ActivityLifecycleCallbacks {

    @Inject
    lateinit var api: ApiService

    private var disposable: Disposable? = null

    private var numStarted = 0

    init {
        App.appComponent?.inject(this)
    }

    private fun onForeground(context: Context) {
        if (Session.needToPromptPin()) {
            PinActivity.launch(context, PinActivity.Action.ENTER)
        }

        if (Prefs.beOnline) {
            startOnline()
        }
    }

    private fun onBackground() {
        stopOnline()
    }

    private fun ignore(activity: Activity?) =
            activity is LoginActivity ||
                    activity is PinActivity ||
                    activity is AlarmActivity

    override fun onActivityStarted(activity: Activity) {
        if (ignore(activity)) return

        if (numStarted == 0) onForeground(activity)
        numStarted++
    }

    override fun onActivityStopped(activity: Activity) {
        if (ignore(activity)) return

        numStarted--
        if (numStarted == 0) onBackground()
    }

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivityDestroyed(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    private fun startOnline() {
        L.tag(TAG_ONLINE).log("start")
        disposable = Flowable.interval(0L, ONLINE_INTERVAL, TimeUnit.SECONDS)
                .flatMap {
                    api.setOnline()
                }
                .onErrorReturn {
                    L.tag(TAG_ONLINE).throwable(it).log("set online error")
                    BaseResponse(response = 0)
                }
                .subscribe { response ->
                    L.tag(TAG_ONLINE).log("set online: ${response.response}")
                }
    }

    private fun stopOnline() {
        if (disposable?.isDisposed == false) {
            disposable?.dispose()
            L.tag(TAG_ONLINE).log("stop")
        }
    }

    companion object {
        private const val TAG_ONLINE = "online"
        private const val ONLINE_INTERVAL = 60L
    }
}