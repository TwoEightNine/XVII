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

package com.twoeightnine.root.xvii

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.twoeightnine.root.xvii.dagger.AppComponent
import com.twoeightnine.root.xvii.dagger.DaggerAppComponent
import com.twoeightnine.root.xvii.dagger.modules.ContextModule
import com.twoeightnine.root.xvii.lg.L
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.utils.*
import global.msnthrp.xvii.data.utils.ContextHolder
import global.msnthrp.xvii.data.utils.ContextProvider
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        ContextHolder.contextProvider = object : ContextProvider {
            override val applicationContext: Context = context
        }

        // here to prevent relaunch of the first activity
        AppCompatDelegate.setDefaultNightMode(when {
            Prefs.isLightTheme -> AppCompatDelegate.MODE_NIGHT_NO
            else -> AppCompatDelegate.MODE_NIGHT_YES
        })

        VibrationHelper.initVibrator(context)
        appComponent = DaggerAppComponent.builder()
                .contextModule(ContextModule(this))
                .build()

        registerActivityLifecycleCallbacks(AppLifecycleTracker())
        ColorManager.init(applicationContext)

        ViewPump.init(ViewPump.builder()
                .addInterceptor(CalligraphyInterceptor(
                        CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/usual.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()
                ))
                .build())

        AsyncUtils.onIoThread({
            EmojiHelper.init()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannels.initChannels(this)
            }

            try {
                StatTool.init(applicationContext)
            } catch (e: Exception) {
                L.tag("stat").warn()
                        .throwable(e)
                        .log("init failed")
            }
        })
    }

    companion object {
        lateinit var context: Context
        var appComponent: AppComponent? = null

        const val VERSION = "5.131"
        const val APP_ID = 6079611
        const val SCOPE_ALL = 471062
        const val REDIRECT_URL_WEB_VIEW = "https://oauth.vk.com/blank.html"
        const val API_URL = "https://api.vk.com/method/"
        const val SHARE_POST = "wall-137238289_390"

        val ID_HASHES = arrayListOf("260ca2827e258c06153e86d121de1094", "44b8e44538545051a8bd710e5e10e5ce", "7c3785059f7ffd4a21d38bd203d13721")
        val ID_SALTS = arrayListOf("iw363c8b6385cy4", "iw57xs57fdvb4en", "i26734c8vb34tr")
        const val GROUP = 137238289
    }
}