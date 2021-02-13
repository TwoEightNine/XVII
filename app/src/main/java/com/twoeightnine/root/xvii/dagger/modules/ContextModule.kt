package com.twoeightnine.root.xvii.dagger.modules

import android.app.Application
import android.content.Context
import com.twoeightnine.root.xvii.chats.tools.ChatStorage
import dagger.Module
import dagger.Provides
import global.msnthrp.xvii.data.db.AppDb
import javax.inject.Singleton

@Module
class ContextModule(private val app: Application) {

    @Provides
    @Singleton
    fun provideContext(): Context = app

    @Provides
    @Singleton
    fun provideAppDb(context: Context): AppDb = AppDb.buildDatabase(context)

    @Provides
    @Singleton
    fun provideChatStorage(context: Context): ChatStorage = ChatStorage(context)

}