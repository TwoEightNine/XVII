package com.twoeightnine.root.xvii.chats.attachments.stickers

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.lg.Lg
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.model.attachments.Sticker
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.utils.*
import com.twoeightnine.root.xvii.views.KeyboardWindow
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.android.synthetic.main.fragment_attachments.view.*
import javax.inject.Inject

class StickersWindow(
        rootView: View,
        context: Context,
        onKeyboardClosed: () -> Unit,
        private val onStickerClicked: (Sticker) -> Unit
) : KeyboardWindow(rootView, context, onKeyboardClosed) {

    @Inject
    lateinit var api: ApiService

    private val availableStorage by lazy {
        StickersStorage(context, StickersStorage.Type.AVAILABLE)
    }

    private val recentStorage by lazy {
        StickersStorage(context, StickersStorage.Type.RECENT)
    }

    private val adapter by lazy {
        StickersAdapter(context, ::onStickerSelected)
    }

    override fun createView(): View {
        val view = View.inflate(context, R.layout.fragment_attachments, null)
        with(view) {
            rvAttachments.layoutManager = GridLayoutManager(context, StickersFragment.SPAN_COUNT)
            rvAttachments.adapter = adapter

            progressBar.show()
            swipeRefresh.setOnRefreshListener {
                loadFromServer()
            }
            Style.forProgressBar(progressBar)
        }
        return view
    }

    override fun onViewCreated() {
        super.onViewCreated()
        App.appComponent?.inject(this)
        loadFromStorage()
    }

    private fun updateList(data: ArrayList<Sticker>) {
        with(contentView) {
            swipeRefresh.isRefreshing = false
            progressBar.hide()
        }
        adapter.update(data)
    }

    @SuppressLint("CheckResult")
    private fun onStickerSelected(sticker: Sticker) {
        onStickerClicked(sticker)
        Single.fromCallable {
            val recent = recentStorage.readFromFile()
            if (sticker in recent) {
                recent.removeAll { it == sticker }
            }
            recent.add(0, sticker)
            recentStorage.writeToFile(recent)
            availableStorage.readFromFile()
        }
                .compose(applySingleSchedulers())
                .subscribe({}) {
                    it.printStackTrace()
                    Lg.i("[stickers] selecting: ${it.message}")
                }
    }

    private fun onErrorOccurred(error: String) {
        showAlert(context, error)
    }

    /**
     * accepts a list fo available stickers, reads recent stickers,
     * creates single list and refreshes ui
     */
    @SuppressLint("CheckResult")
    private fun updateStickers(available: ArrayList<Sticker>) {
        Single.fromCallable {
            val recent = recentStorage.readFromFile()
            available.removeAll(recent)
            recent.addAll(available)
            recent
        }
                .compose(applySingleSchedulers())
                .subscribe(::updateList) {
                    it.printStackTrace()
                    Lg.i("[stickers] updating: ${it.message}")
                    onErrorOccurred(it.message ?: "No stickers")
                }
    }

    @SuppressLint("CheckResult")
    private fun loadFromStorage() {
        Single.fromCallable {
            availableStorage.readFromFile()
        }
                .compose(applySingleSchedulers())
                .subscribe({ stickers ->
                    if (stickers.isNotEmpty()) {
                        updateStickers(stickers)
                    } else {
                        loadFromServer()
                    }
                }) {
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
                            stickers.add(Sticker(it))
                        }
                    }
                    val result = ArrayList(stickers.sortedBy { it.id }.distinctBy { it.id })
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

    override fun getAdditionalHeight() = 0
}