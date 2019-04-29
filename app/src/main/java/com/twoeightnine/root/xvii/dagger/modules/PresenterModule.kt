package com.twoeightnine.root.xvii.dagger.modules

import com.twoeightnine.root.xvii.mvp.presenter.ChatFragmentPresenter
import com.twoeightnine.root.xvii.network.ApiService
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class PresenterModule {

    @Provides
    @Singleton
    fun provideChatPresenter(api: ApiService): ChatFragmentPresenter = ChatFragmentPresenter(api)

}