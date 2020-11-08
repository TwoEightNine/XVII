package com.twoeightnine.root.xvii.utils

import android.graphics.drawable.Drawable
import android.os.SystemClock
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.RequestBuilder
import com.jakewharton.rxbinding.widget.RxTextView
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.crypto.CryptoEngine
import com.twoeightnine.root.xvii.network.response.BaseResponse
import com.twoeightnine.root.xvii.network.response.Error
import global.msnthrp.xvii.uikit.extensions.load
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

const val CHAT_ID_START = 2000000000

fun String.matchesXviiCipher(): Boolean {
    val prefix = CryptoEngine.DATA_PREFIX
    val postfix = CryptoEngine.DATA_POSTFIX
    return length > prefix.length + postfix.length &&
            substring(0, prefix.length) == prefix &&
            substring(length - postfix.length) == postfix
}

fun String.matchesXviiKeyEx(): Boolean {
    val prefix = CryptoEngine.KEY_PREFIX
    val postfix = CryptoEngine.KEY_POSTFIX
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
                    val errCode = resp.error.code
                    when (errCode) {
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

fun ImageView.load(url: String?, placeholder: Boolean = true,
                   block: RequestBuilder<Drawable>.() -> RequestBuilder<Drawable> = { this }) {
    val stubColorUrl = ColorManager.getPhotoStub()
    val placeholderColor = ContextCompat.getColor(context, R.color.placeholder)
    load(url, stubColorUrl, placeholderColor, placeholder, block)
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
