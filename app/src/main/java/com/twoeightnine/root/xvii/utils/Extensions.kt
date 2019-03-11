package com.twoeightnine.root.xvii.utils

import android.os.SystemClock
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import com.twoeightnine.root.xvii.model.response.Error
import com.twoeightnine.root.xvii.response.ServerResponse
import com.twoeightnine.root.xvii.utils.crypto.CryptoUtil
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable

fun String.matchesXviiKey() = length > CryptoUtil.PREFIX.length + CryptoUtil.POSTFIX.length &&
        this.substring(0, CryptoUtil.PREFIX.length) == CryptoUtil.PREFIX &&
        this.substring(length - CryptoUtil.POSTFIX.length) == CryptoUtil.POSTFIX


fun <T> Flowable<ServerResponse<T>>.subscribeSmart(response: (T) -> Unit,
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
                newtError.invoke(err.message ?: "null")
            })
}

fun View.setVisible(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

fun View.getVisible() = visibility == View.VISIBLE

fun View.toggle() {
    setVisible(!getVisible())
}

fun View.show() = setVisible(true)

fun View.hide() = setVisible(false)

fun ImageView.load(url: String?, block: RequestCreator.() -> RequestCreator = { this }) {
    Picasso.get()
            .load(url)
            .block()
            .into(this)
}

fun EditText.asText() = text.toString()

fun EditText.clear() {
    setText("")
}