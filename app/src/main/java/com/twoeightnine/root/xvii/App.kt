package com.twoeightnine.root.xvii

import android.app.Application
import android.content.Context
import com.twoeightnine.root.xvii.dagger.AppComponent
import com.twoeightnine.root.xvii.dagger.DaggerAppComponent
import com.twoeightnine.root.xvii.dagger.modules.ContextModule
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.utils.AppLifecycleTracker
import io.realm.Realm
import io.realm.RealmConfiguration

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(AppLifecycleTracker())
        context = applicationContext
        appComponent = DaggerAppComponent.builder()
                .contextModule(ContextModule(this))
                .build()
        Style.init(applicationContext)

        Realm.init(applicationContext)
        val realmConfig = RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .name("realmDb")
                .build()
        Realm.setDefaultConfiguration(realmConfig)

//        try {
//            StatTool.init(applicationContext)
//        } catch (e: Exception) {
//            Lg.wtf("[stat] init failed")
//        }
    }

    companion object {
        lateinit var context: Context
        var appComponent: AppComponent? = null

        const val VERSION = "5.63"
        const val APP_ID = 6079611
        const val SCOPE_ALL = 471062
        const val REDIRECT_URL = "https://oauth.vk.com/blank.html"
        const val API_URL = "https://api.vk.com/method/"
        const val PHOTO_STUB = "https://upload.wikimedia.org/wikipedia/commons/b/b3/Solid_gray.png"
        const val SHARE_POST = "wall-137238289_146"

        val ID_HASHES = arrayListOf("260ca2827e258c06153e86d121de1094", "44b8e44538545051a8bd710e5e10e5ce", "7c3785059f7ffd4a21d38bd203d13721")
        val ID_SALTS = arrayListOf("iw363c8b6385cy4", "iw57xs57fdvb4en", "i26734c8vb34tr")
        const val GROUP = 137238289
    }
}