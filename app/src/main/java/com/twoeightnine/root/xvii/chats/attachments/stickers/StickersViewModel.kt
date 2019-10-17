package com.twoeightnine.root.xvii.chats.attachments.stickers

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import com.twoeightnine.root.xvii.lg.Lg
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.model.WrappedLiveData
import com.twoeightnine.root.xvii.model.WrappedMutableLiveData
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.model.attachments.Sticker
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.utils.applyCompletableSchedulers
import com.twoeightnine.root.xvii.utils.applySingleSchedulers
import com.twoeightnine.root.xvii.utils.subscribeSmart
import io.reactivex.Completable
import io.reactivex.Single

class StickersViewModel(
        private val api: ApiService,
        private val context: Context
) : ViewModel() {

    private val stickersLiveData = WrappedMutableLiveData<ArrayList<Sticker>>()

    private val availableStorage by lazy {
        StickersStorage(context, StickersStorage.Type.AVAILABLE)
    }

    private val recentStorage by lazy {
        StickersStorage(context, StickersStorage.Type.RECENT)
    }

    fun getStickers() = stickersLiveData as WrappedLiveData<ArrayList<Sticker>>

    fun loadStickers(refresh: Boolean = false) {
        if (refresh) {
            loadFromServer()
        } else {
            loadFromStorage()
        }
    }

    @SuppressLint("CheckResult")
    fun onStickerSelected(sticker: Sticker) {
        Single.fromCallable {
            val recent = recentStorage.readFromFile()
            if (recent.isEmpty()) {
                recent.addAll(Prefs.recentStickers.map { Sticker(it) })
            }
            if (sticker in recent) {
                recent.removeAll { it == sticker }
            }
            recent.add(0, sticker)
            recentStorage.writeToFile(recent)
            availableStorage.readFromFile()
        }
                .compose(applySingleSchedulers())
                .subscribe(::updateStickers) {
                    it.printStackTrace()
                    Lg.i("[stickers] selecting: ${it.message}")
                }
    }

    private fun onErrorOccurred(error: String) {
        stickersLiveData.value = Wrapper(error = error)
    }

    @SuppressLint("CheckResult")
    private fun updateStickers(available: ArrayList<Sticker>) {
        Single.fromCallable {
            val recent = recentStorage.readFromFile()
            if (recent.isEmpty()) {
                recent.addAll(Prefs.recentStickers.map { Sticker(it) })
            }
            available.removeAll(recent)
            recent.addAll(available)
            recent
        }
                .compose(applySingleSchedulers())
                .subscribe({ stickers ->
                    stickersLiveData.value = Wrapper(stickers)
                }, {
                    it.printStackTrace()
                    Lg.i("[stickers] updating: ${it.message}")
                    onErrorOccurred(it.message ?: "No stickers")
                })
    }

    @SuppressLint("CheckResult")
    private fun loadFromStorage() {
        Single.fromCallable {
            availableStorage.readFromFile()
        }
                .compose(applySingleSchedulers())
                .subscribe(::updateStickers) {
                    it.printStackTrace()
                    Lg.i("[stickers] loading from storage: ${it.message}")
                    loadFromServer()
                }
    }

    private fun loadFromServer() {
        api.getStickers()
                .subscribeSmart({ response ->
                    val stickers = arrayListOf<Sticker>()
                    response.dictionary?.forEach { mind ->
                        mind.userStickers?.forEach {
                            stickers.add(it)
                        }
                    }
                    val result = ArrayList(stickers.sortedBy { it.stickerId }.distinctBy { it.stickerId })
                    updateStickers(result)
                    saveStickers(result)
                }, ::onErrorOccurred)
    }

    private fun saveStickers(stickers: ArrayList<Sticker>) {
        Completable.fromCallable {
            availableStorage.writeToFile(stickers)
        }
                .compose(applyCompletableSchedulers())
                .subscribe()
    }
}