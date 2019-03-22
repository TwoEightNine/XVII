package com.twoeightnine.root.xvii.dagger.modules

import android.app.Application
import android.content.Context
import com.twoeightnine.root.xvii.db.AppDb
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ContextModule(private val app: Application) {

    @Provides
    @Singleton
    fun provideContext(): Context = app

    @Provides
    @Singleton
    fun provideAppDb(context: Context): AppDb = AppDb.buildDatabase(context)

}