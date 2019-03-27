package com.twoeightnine.root.xvii.utils

import android.os.SystemClock
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.network.response.BaseResponse
import com.twoeightnine.root.xvii.network.response.Error
import com.twoeightnine.root.xvii.utils.crypto.CryptoUtil
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable

const val CHAT_ID_START = 2000000000

fun String.matchesXviiKey() = length > CryptoUtil.PREFIX.length + CryptoUtil.POSTFIX.length &&
        this.substring(0, CryptoUtil.PREFIX.length) == CryptoUtil.PREFIX &&
        this.substring(length - CryptoUtil.POSTFIX.length) == CryptoUtil.POSTFIX


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

fun View.setVisible(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

fun View.getVisible() = visibility == View.VISIBLE

fun View.toggle() {
    setVisible(!getVisible())
}

fun View.show() = setVisible(true)

fun View.hide() = setVisible(false)

fun ImageView.load(url: String?, placeholder: Boolean = true,
                   block: RequestCreator.() -> RequestCreator = { this }) {
    val rc = Picasso.get().load(url)
    if (placeholder) {
        rc.placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
    }
    rc.block().into(this)
}

fun ImageView.loadRounded(url: String?) {
    if (url == null) return
    Picasso.get()
            .loadRounded(url)
            .into(this)
}

fun Picasso.loadRounded(url: String?): RequestCreator {
    return this
            .load(url)
            .transform(RoundedTransformation())
            .placeholder(R.drawable.placeholder_rounded)
            .error(R.drawable.placeholder_rounded)

}

fun EditText.asText() = text.toString()

fun EditText.clear() {
    setText("")
}