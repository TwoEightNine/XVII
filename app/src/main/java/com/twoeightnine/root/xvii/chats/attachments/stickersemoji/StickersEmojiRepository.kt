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

package com.twoeightnine.root.xvii.chats.attachments.stickersemoji

import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.lg.L
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.utils.applyCompletableSchedulers
import com.twoeightnine.root.xvii.utils.applySchedulers
import com.twoeightnine.root.xvii.utils.applySingleSchedulers
import com.twoeightnine.root.xvii.utils.time
import global.msnthrp.xvii.data.db.AppDb
import global.msnthrp.xvii.data.stickersemoji.model.*
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

    fun setStickerUsed(stickerId: Int) {
        appDb.stickersDao()
                .updateStickerUsed(StickerUsage(stickerId, time()))
                .compose(applyCompletableSchedulers())
                .subscribe()
                .add()
    }

    /**
     * loads stickers as sticker packs
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
                    L.tag(TAG)
                            .throwable(error)
                            .log("error loading packs")
                })
                .add()
    }

    /**
     * load list of stickers directly from db
     */
    fun loadRawStickersFromDb(onLoaded: (List<Sticker>) -> Unit) {
        loadStickersFromDb()
                .compose(applySingleSchedulers())
                .subscribe(onLoaded) { error ->
                    L.tag(TAG)
                            .throwable(error)
                            .log("error loading raw stickers")
                }.add()
    }

    fun setEmojiUsed(emojiCode: String) {
        appDb.emojisDao()
                .updateStickerUsed(EmojiUsage(emojiCode, time()))
                .compose(applyCompletableSchedulers())
                .subscribe()
                .add()
    }

    fun loadEmojis(onLoaded: (List<EmojiPack>) -> Unit) {
        loadEmojisFromDb()
                .flatMap(this::createEmojiPacks)
                .compose(applySingleSchedulers())
                .subscribe(onLoaded) { error ->
                    L.tag(TAG)
                            .throwable(error)
                            .log("error loading emoji packs")
                }
                .add()
    }

    fun loadRawEmojis(onLoaded: (List<Emoji>) -> Unit) {
        loadEmojisFromDb()
                .compose(applySingleSchedulers())
                .subscribe(onLoaded) { error ->
                    L.tag(TAG)
                            .throwable(error)
                            .log("error loading raw emojis")
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
                        L.tag(TAG)
                                .throwable(it)
                                .log("error loading stickers from net")
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
                        L.tag(TAG)
                                .throwable(it)
                                .log("error loading sticker keywords from net")
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
                packs.sortBy { pack -> pack.stickers.map { it.id }.median() }
                packs.toList()
            }.compose(applySingleSchedulers())

    private fun loadEmojisFromDb(): Single<List<Emoji>> =
            appDb.emojisDao()
                    .getAllEmojis()
                    // dirty hack
                    .map { emojis -> emojis.map { it.copy(code = it.code.toUnicodeEmoji()) } }
                    .onErrorReturn { error ->
                        L.tag(TAG)
                                .throwable(error)
                                .log("error loading emojis")
                        listOf()
                    }
                    .compose(applySingleSchedulers())

    private fun loadRecentEmojis(emojis: List<Emoji>): Single<List<Emoji>> =
            appDb.emojisDao()
                    .getRecentEmojis()
                    .map { recentCodes ->
                        val recentEmojis = arrayListOf<Emoji>()
                        for (emoji in emojis) {

                            if (emoji.code in recentCodes) {
                                recentEmojis.add(emoji)
                            }
                        }
                        recentEmojis.sortBy { recentCodes.indexOf(it.code) }
                        recentEmojis
                    }

    private fun convertEmojisToPacks(
            emojis: List<Emoji>,
            recent: List<Emoji>
    ): Single<List<EmojiPack>> =
            Single.fromCallable {
                val packs = arrayListOf<EmojiPack>()
                val packsMap = hashMapOf<String, ArrayList<Emoji>>()
                for (emoji in emojis) {
                    if (emoji.packName !in packsMap) {
                        packsMap[emoji.packName] = arrayListOf()
                    }
                    packsMap[emoji.packName]?.add(emoji)
                }
                packsMap.forEach { entry ->
                    packs.add(EmojiPack(entry.key, entry.value))
                }
                packs.add(EmojiPack(null, recent))
                packs.toList()
            }.compose(applySingleSchedulers())

    private fun createEmojiPacks(emojis: List<Emoji>): Single<List<EmojiPack>> =
            loadRecentEmojis(emojis)
                    .flatMap { recent ->
                        convertEmojisToPacks(emojis, recent)
                    }
                    .compose(applySingleSchedulers())

    private fun String.toUnicodeEmoji(): String {
        val clean = replace("\\u", "")
        var result = ""
        var cnt = 0
        while (cnt < clean.length) {
            val num = Integer.parseInt(clean.substring(cnt, cnt + 4), 16)
            result += num.toChar()
            cnt += 4
        }
        return result
    }

    fun destroy() {
        compositeDisposable.dispose()
    }

    private fun Disposable.add() {
        compositeDisposable.add(this)
    }

    private fun List<Int>.median(): Int {
        if (isEmpty()) return 0
        val sorted = sorted()
        val centerIndex = sorted.size / 2
        return if (sorted.size % 2 == 0) {
            (sorted[centerIndex] + sorted[centerIndex - 1]) / 2
        } else {
            sorted[centerIndex]
        }
    }

    companion object {
        const val TAG = "stickers and emojis"
    }

}