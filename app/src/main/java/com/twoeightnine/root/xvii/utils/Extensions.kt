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

package com.twoeightnine.root.xvii.utils

import android.os.SystemClock
import android.widget.TextView
import com.jakewharton.rxbinding.widget.RxTextView
import com.twoeightnine.root.xvii.network.response.BaseResponse
import com.twoeightnine.root.xvii.network.response.Error
import global.msnthrp.xvii.core.crypto.CryptoConsts
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

const val CHAT_ID_START = 2000000000

private val reloginHandler by lazy { ReloginHandler() }

fun String.matchesXviiCipher(): Boolean {
    val prefix = CryptoConsts.DATA_PREFIX
    val postfix = CryptoConsts.DATA_POSTFIX
    return length > prefix.length + postfix.length &&
            substring(0, prefix.length) == prefix &&
            substring(length - postfix.length) == postfix
}

fun String.matchesXviiKeyEx(): Boolean {
    val prefix = CryptoConsts.KEY_PREFIX
    val postfix = CryptoConsts.KEY_POSTFIX
    return length > prefix.length + postfix.length &&
            substring(0, prefix.length) == prefix &&
            substring(length - postfix.length) == postfix
}

fun Int.matchesUserId() = this in 0..CHAT_ID_START

fun Int.matchesGroupId() = this < 0

fun Int.matchesChatId() = this > CHAT_ID_START

fun Int.asChatId() = this - CHAT_ID_START

fun Int.asChatPeerId() = this + CHAT_ID_START

fun <T> Flowable<BaseResponse<T>>.subscribeSmart(response: (T) -> Unit,
                                                 error: (String) -> Unit,
                                                 newtError: (String) -> Unit = error): Disposable {
    return this.compose(applySchedulers())
            .subscribe({ resp ->
                if (resp.response != null) {
                    response.invoke(resp.response)
                } else if (resp.error != null) {
                    val errorMsg = resp.error.friendlyMessage()
                    when (resp.error.code) {
                        Error.AUTH_FAILED -> reloginHandler.onAuthFailed()
                        Error.TOO_MANY -> {
                            Thread {
                                SystemClock.sleep(330)
                                this.subscribeSmart(response, error)
                            }.start()
                        }
                        else -> error.invoke(errorMsg ?: "null")
                    }
                }
            }, { err ->
                err.printStackTrace()
                newtError.invoke(err.message ?: "null")
            })
}

fun TextView.subscribeSearch(
        allowEmpty: Boolean,
        onNext: (String) -> Unit
): Subscription = RxTextView.textChanges(this)
        .filter { allowEmpty || it.isNotBlank() }
        .debounce(300, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .map { it.toString() }
        .subscribe(onNext) {
            it.printStackTrace()
        }
