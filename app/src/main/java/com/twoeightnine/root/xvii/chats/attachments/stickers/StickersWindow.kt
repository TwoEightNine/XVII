package com.twoeightnine.root.xvii.chats.attachments.stickers

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.lg.Lg
import com.twoeightnine.root.xvii.model.attachments.Sticker
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.utils.*
import com.twoeightnine.root.xvii.views.KeyboardWindow
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
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

    private var disposable: Disposable? = null

    private val availableStorage by lazy {
        StickersStorage(context, StickersStorage.Type.AVAILABLE)
    }

    private val recentStorage by lazy {
        StickersStorage(context, StickersStorage.Type.RECENT)
    }

    private val adapter by lazy {
        StickersAdapter(context, ::onStickerSelected, ::onStickerLongClicked)
    }

    override fun createView(): View {
        val view = View.inflate(context, R.layout.fragment_attachments, null)
        with(view) {
            rvAttachments.layoutManager = GridLayoutManager(context, StickersFragment.SPAN_COUNT)
            rvAttachments.adapter = adapter

            progressBar.show()
            swipeRefresh.setOnRefreshListener {
                loadStickers(forceFetch = true)
            }
            progressBar.stylize()
        }
        return view
    }

    override fun onViewCreated() {
        super.onViewCreated()
        App.appComponent?.inject(this)
        loadStickers()
        setOnDismissListener { disposable?.dispose() }
    }

    private fun updateList(data: List<Sticker>) {
        with(contentView) {
            swipeRefresh.isRefreshing = false
            progressBar.hide()
        }
        adapter.update(data)
    }

    @SuppressLint("CheckResult")
    private fun onStickerSelected(sticker: Sticker) {
        onStickerClicked(sticker)
        Completable.fromCallable {
            val recent = recentStorage.readFromFile()
            if (sticker in recent) {
                recent.removeAll { it == sticker }
            }
            recent.add(0, sticker)
            recentStorage.writeToFile(recent)
        }
                .compose(applyCompletableSchedulers())
                .subscribe({}) {
                    it.printStackTrace()
                    Lg.wtf("[stickers] selecting: ${it.message}")
                }
    }

    private fun onStickerLongClicked(sticker: Sticker) {
//        StickerPreviewDialog(context, sticker) { stickerId, keywords ->
//
//        }.show()
    }

    private fun onErrorOccurred(error: String) {
        showAlert(context, error)
    }

    /**
     * saves stickers to [availableStorage]
     */
    private fun saveAvailable(stickers: List<Sticker>) =
            Single.fromCallable {
                availableStorage.writeToFile(ArrayList(stickers))
                stickers
            }

    /**
     * loads stickers from server, saves to [availableStorage]
     */
    private fun loadStickersFromServer() =
            api.getStickers()
                    .compose(applySchedulers())
                    .map { it.response?.getStickers() ?: arrayListOf() }
                    .singleOrError()
                    .flatMap(::saveAvailable)

    /**
     * adds recent stickers to available stickers
     * @param stickers available
     */
    private fun extendAvailableWithRecent(stickers: List<Sticker>) =
            Single.fromCallable {
                val available = ArrayList(stickers)
                val recent = recentStorage.readFromFile()
                available.addAll(0, recent)
                available
            }

    /**
     * loads stickers and updates ui
     * @param forceFetch if true loads from server anyway
     */
    private fun loadStickers(forceFetch: Boolean = false) {
        disposable?.dispose()
        disposable = Single.fromCallable {
            availableStorage.readFromFile()
        }
                .compose(applySingleSchedulers())
                .flatMap { stickers ->
                    if (stickers.isEmpty() || forceFetch) {
                        loadStickersFromServer()
                    } else {
                        Single.just(stickers)
                    }
                }
                .flatMap(::extendAvailableWithRecent)
                .subscribe(::updateList) {
                    it.printStackTrace()
                    Lg.wtf("[stickers] updating: ${it.message}")
                    onErrorOccurred(it.message ?: "No stickers")
                    updateList(arrayListOf())
                }
    }

    override fun getAdditionalHeight() = 0
}