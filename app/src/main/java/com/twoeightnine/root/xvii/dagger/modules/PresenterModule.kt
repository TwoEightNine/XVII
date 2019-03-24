package com.twoeightnine.root.xvii.dagger.modules

import com.twoeightnine.root.xvii.mvp.presenter.ChatFragmentPresenter
import com.twoeightnine.root.xvii.mvp.presenter.ImportantFragmentPresenter
import com.twoeightnine.root.xvii.mvp.presenter.SearchMessagesFragmentPresenter
import com.twoeightnine.root.xvii.mvp.presenter.SearchUsersPresenter
import com.twoeightnine.root.xvii.network.ApiService
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class PresenterModule {

    @Provides
    @Singleton
    fun provideChatPresenter(api: ApiService): ChatFragmentPresenter = ChatFragmentPresenter(api)

    @Provides
    @Singleton
    fun provideSearchMessagesPresenter(api: ApiService): SearchMessagesFragmentPresenter = SearchMessagesFragmentPresenter(api)

    @Provides
    @Singleton
    fun provideSearchUsersPresenter(api: ApiService): SearchUsersPresenter = SearchUsersPresenter(api)

    @Provides
    @Singleton
    fun provideImportantPresenter(api: ApiService): ImportantFragmentPresenter = ImportantFragmentPresenter(api)

}