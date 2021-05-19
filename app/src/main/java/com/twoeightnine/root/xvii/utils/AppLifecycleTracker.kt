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
import com.twoeightnine.root.xvii.pin.fake.diagnostics.DiagnosticsActivity
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AppLifecycleTracker : Application.ActivityLifecycleCallbacks {

    @Inject
    lateinit var api: ApiService

    private var disposable: Disposable? = null

    private var numStarted = 0

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
            activity is LoginActivity
                    || activity is PinActivity
                    || activity is AlarmActivity
                    || activity is DiagnosticsActivity

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
        initApiIfNeeded()
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

    private fun initApiIfNeeded() {
        if (!::api.isInitialized) {
            App.appComponent?.inject(this)
        }
    }

    companion object {
        private const val TAG_ONLINE = "online"
        private const val ONLINE_INTERVAL = 60L
    }
}