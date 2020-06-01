package com.twoeightnine.root.xvii.chats.attachments.stickersemoji

import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.chats.attachments.stickersemoji.model.Sticker
import com.twoeightnine.root.xvii.chats.attachments.stickersemoji.model.StickerPack
import com.twoeightnine.root.xvii.chats.attachments.stickersemoji.model.StickerUsage
import com.twoeightnine.root.xvii.db.AppDb
import com.twoeightnine.root.xvii.lg.Lg
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.utils.applyCompletableSchedulers
import com.twoeightnine.root.xvii.utils.applySchedulers
import com.twoeightnine.root.xvii.utils.applySingleSchedulers
import com.twoeightnine.root.xvii.utils.time
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class StickersEmojiRepository {

    @Inject
    lateinit var api: ApiService

    @Inject
    lateinit var appDb: AppDb

    private val compositeDisposable = CompositeDisposable()

    init {
        App.appComponent?.inject(this)
    }

    fun setStickerUsed(sticker: Sticker) {
        appDb.stickersDao()
                .updateStickerUsed(StickerUsage(sticker.id, time()))
                .compose(applyCompletableSchedulers())
                .subscribe()
                .add()
    }

    /**
     * loads stickers
     * @param forceLoad true if load from net anyway
     */
    fun loadStickers(forceLoad: Boolean = false, onLoaded: (List<StickerPack>) -> Unit) {
        // load from db
        val fromDb = loadStickersFromDb()
                .flatMap { stickers ->
                    if (stickers.isEmpty()) { // if empty, load from net
                        loadFromNetAndCache()
                    } else { // else continue with data from db
                        Single.just(stickers)
                    }
                }
        val fromNet = loadFromNetAndCache()

        (if (forceLoad) fromNet else fromDb)
                .flatMap(this::createPacks)
                .compose(applySingleSchedulers())
                .subscribe(onLoaded, { error ->
                    error.printStackTrace()
                    Lg.wtf("error loading packs: ${error.message}")
                })
                .add()
    }

    fun loadRawStickersFromDb(onLoaded: (List<Sticker>) -> Unit) {
        loadStickersFromDb()
                .compose(applySingleSchedulers())
                .subscribe(onLoaded) { error ->
                    error.printStackTrace()
                    Lg.wtf("error loading raw stickers: ${error.message}")
                }.add()
    }

    private fun loadStickersFromDb(): Single<List<Sticker>> =
            appDb.stickersDao()
                    .getAllStickers()

    private fun loadFromNetAndCache(): Single<List<Sticker>> =
            loadStickersFromNet()
                    .flatMap { stickers ->
                        loadKeywordsFromNet()
                                .map { idToWordsMap ->
                                    val filledStickers = arrayListOf<Sticker>()
                                    for (sticker in stickers) {
                                        filledStickers.add(if (sticker.id in idToWordsMap) {
                                            sticker.copy(
                                                    keyWords = idToWordsMap[sticker.id]
                                                            ?.joinToString(separator = ",") ?: ""
                                            )
                                        } else {
                                            sticker
                                        })
                                    }
                                    filledStickers
                                }
                    }
                    .flatMap(::saveStickers)

    private fun loadStickersFromNet(): Single<ArrayList<Sticker>> =
            api.getStickers()
                    .map { response ->
                        response.response?.items
                    }
                    .map { packs ->
                        val stickers = arrayListOf<Sticker>()
                        for (pack in packs) {
                            for (sticker in pack.stickers) {
                                if (sticker.isAllowed) {
                                    stickers.add(Sticker(
                                            id = sticker.stickerId,
                                            keyWords = "",
                                            keyWordsCustom = "",
                                            packName = pack.name
                                    ))
                                }
                            }
                        }
                        stickers
                    }
                    .onErrorReturn {
                        it.printStackTrace()
                        Lg.wtf("error loading stickers from net: ${it.message}")
                        arrayListOf()
                    }
                    .compose(applySchedulers())
                    .singleOrError()

    private fun loadKeywordsFromNet(): Single<Map<Int, List<String>>> =
            api.getStickersKeywords()
                    .map { response ->
                        if (response.response != null) {
                            response.response.getStickerIdToWordsMap()
                        } else {
                            hashMapOf()
                        }
                    }
                    .onErrorReturn {
                        it.printStackTrace()
                        Lg.wtf("error loading sticker keywords from net: ${it.message}")
                        hashMapOf()
                    }
                    .compose(applySchedulers())
                    .singleOrError()

    private fun saveStickers(stickers: List<Sticker>): Single<List<Sticker>> =
            appDb.stickersDao()
                    .saveStickers(stickers)
                    .compose(applyCompletableSchedulers())
                    .toSingleDefault(stickers)

    private fun createPacks(stickers: List<Sticker>): Single<List<StickerPack>> =
            loadRecentFromDb(stickers)
                    .flatMap { recentStickers ->
                        convertStickersToPacks(stickers, recentStickers)
                    }
                    .compose(applySingleSchedulers())

    private fun loadRecentFromDb(stickers: List<Sticker>): Single<List<Sticker>> =
            appDb.stickersDao()
                    .getRecentStickers()
                    .map { recentIds ->
                        val recentStickers = arrayListOf<Sticker>()
                        for (sticker in stickers) {
                            if (sticker.id in recentIds) {
                                recentStickers.add(sticker)
                            }
                        }
                        recentStickers.sortBy { recentIds.indexOf(it.id) }
                        recentStickers
                    }

    private fun convertStickersToPacks(
            stickers: List<Sticker>,
            recent: List<Sticker>
    ): Single<List<StickerPack>> =
            Single.fromCallable {
                val packs = arrayListOf<StickerPack>()
                val packsMap = hashMapOf<String, ArrayList<Sticker>>()
                for (sticker in stickers) {
                    if (sticker.packName !in packsMap) {
                        packsMap[sticker.packName] = arrayListOf()
                    }
                    packsMap[sticker.packName]?.add(sticker)
                }
                packsMap.forEach { entry ->
                    packs.add(StickerPack(entry.key, entry.value))
                }
                packs.add(StickerPack(null, recent))
                packs.sortBy { it.stickers.getOrNull(0)?.id ?: 0 }
                packs.toList()
            }.compose(applySingleSchedulers())

    fun destroy() {
        compositeDisposable.dispose()
    }

    private fun Disposable.add() {
        compositeDisposable.add(this)
    }

}