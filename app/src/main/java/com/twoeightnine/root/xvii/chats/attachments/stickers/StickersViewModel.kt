package com.twoeightnine.root.xvii.chats.attachments.stickers

import android.annotation.SuppressLint
import android.content.Context
import com.twoeightnine.root.xvii.chats.attachments.base.BaseAttachViewModel
import com.twoeightnine.root.xvii.model.Attachment
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.utils.applyCompletableSchedulers
import com.twoeightnine.root.xvii.utils.applySingleSchedulers
import com.twoeightnine.root.xvii.utils.subscribeSmart
import io.reactivex.Completable
import io.reactivex.Single

class StickersViewModel(
        private val api: ApiService,
        private val context: Context
) : BaseAttachViewModel<Attachment.Sticker>() {

    private val storage by lazy {
        StickersStorage(context)
    }

    override fun loadAttach(offset: Int) {
        if (offset != 0) {
            attachLiveData.value = Wrapper(attachLiveData.value?.data ?: return)
            return
        }

        loadFromStorage { stickers ->
            if (stickers.isNotEmpty()) {
                onAttachmentsLoaded(offset, ArrayList(stickers))
            } else {
                loadFromServer {
                    val loaded = ArrayList(it)
                    onAttachmentsLoaded(offset, ArrayList(it))
                    saveStickers(loaded)
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun loadFromStorage(onLoaded: (List<Attachment.Sticker>) -> Unit) {
        Single.fromCallable {
            storage.readFromFile()
        }
                .compose(applySingleSchedulers())
                .subscribe(onLoaded, {
                    it.printStackTrace()
                    onErrorOccurred(it.message ?: "")
                })
    }

    private fun loadFromServer(onLoaded: (List<Attachment.Sticker>) -> Unit) {
        api.getStickers()
                .subscribeSmart({ response ->
                    val stickers = arrayListOf<Attachment.Sticker>()
                    response.dictionary?.forEach { mind ->
                        mind.userStickers?.forEach {
                            stickers.add(Attachment.Sticker(it))
                        }
                    }
                    onLoaded(stickers.sortedBy { it.id }.distinctBy { it.id })
                }, ::onErrorOccurred)
    }

    private fun saveStickers(stickers: ArrayList<Attachment.Sticker>) {
        Completable.fromCallable {
            storage.writeToFile(stickers)
        }
                .compose(applyCompletableSchedulers())
                .subscribe()
    }
}