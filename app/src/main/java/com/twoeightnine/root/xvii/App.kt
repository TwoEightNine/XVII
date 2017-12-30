package com.twoeightnine.root.xvii

import android.app.Application
import android.content.Context
import com.twoeightnine.root.xvii.consts.Api
import com.twoeightnine.root.xvii.dagger.AppComponent
import com.twoeightnine.root.xvii.dagger.DaggerAppComponent
import com.twoeightnine.root.xvii.dagger.modules.ContextModule
import com.twoeightnine.root.xvii.managers.Lg
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.utils.md5
import io.realm.Realm
import io.realm.RealmConfiguration

class App : Application() {


    override fun onCreate() {
        super.onCreate()
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

    }


    companion object {
        lateinit var context: Context
        var appComponent: AppComponent? = null
    }
}