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

package com.twoeightnine.root.xvii.features.general

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.chats.attachments.stickersemoji.StickersEmojiRepository
import com.twoeightnine.root.xvii.lg.L
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.utils.applySingleSchedulers
import com.twoeightnine.root.xvii.utils.getCacheSize
import com.twoeightnine.root.xvii.utils.subscribeSmart
import global.msnthrp.xvii.data.db.AppDb
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class GeneralViewModel : ViewModel() {

    @Inject
    lateinit var applicationContext: Context

    @Inject
    lateinit var appDb: AppDb

    @Inject
    lateinit var api: ApiService

    private var disposable: Disposable? = null

    private val cacheSizeLiveData = MutableLiveData<Long>()
    private val cacheClearedLiveData = MutableLiveData<Boolean>()
    private val stickersRefreshingLiveData = MutableLiveData<Boolean>()
    private val hideStatusLiveData = MutableLiveData<Wrapper<Boolean>>()

    val cacheSize: LiveData<Long>
        get() = cacheSizeLiveData

    val hideStatus: LiveData<Wrapper<Boolean>>
        get() = hideStatusLiveData

    val stickersRefreshing: LiveData<Boolean>
        get() = stickersRefreshingLiveData

    init {
        App.appComponent?.inject(this)
    }

    fun setHideMyStatus(hide: Boolean) {
        api.setPrivacy(KEY, if (hide) VALUE_ENABLED else VALUE_DISABLED)
                .subscribeSmart({
                    hideStatusLiveData.value = Wrapper(hide)
                }, {
                    hideStatusLiveData.value = Wrapper(error = it)
                })
    }

    fun calculateCacheSize() {
        disposable?.dispose()
        disposable = Single.fromCallable {
            getCacheSize(applicationContext)
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn { 0L }
                .subscribe { size ->
                    cacheSizeLiveData.value = size
                }
    }

    fun clearCache() {
        disposable?.dispose()
        disposable = Single.fromCallable {
            com.twoeightnine.root.xvii.utils.clearCache(applicationContext)
            true
        }
                .compose(applySingleSchedulers())
                .onErrorReturn { false }
                .subscribe { success ->
                    cacheClearedLiveData.value = success
                    if (success) {
                        calculateCacheSize()
                    }
                }
    }

    fun refreshStickers() {
        stickersRefreshingLiveData.value = true
        disposable?.dispose()
        disposable = appDb.stickersDao()
                .clearStickers()
                .onErrorComplete()
                .subscribe {
                    StickersEmojiRepository()
                            .loadStickers(forceLoad = true) {
                                stickersRefreshingLiveData.value = false
                                L.tag("stickers")
                                        .log("refreshed")
                            }
                }
    }

    companion object {
        const val KEY = "online"
        const val VALUE_ENABLED = "only_me"
        const val VALUE_DISABLED = "all"
    }

}