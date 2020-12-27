package com.twoeightnine.root.xvii.utils

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.graphics.drawable.ColorDrawable
import android.os.SystemClock
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.jakewharton.rxbinding.widget.RxTextView
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.crypto.CryptoEngine
import com.twoeightnine.root.xvii.network.response.BaseResponse
import com.twoeightnine.root.xvii.network.response.Error
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

fun View.setVisible(visible: Boolean) {
    visibility = when {
        visible -> View.VISIBLE
        else -> View.GONE
    }
}

fun View.setVisibleWithInvis(visible: Boolean) {
    visibility = when {
        visible -> View.VISIBLE
        else -> View.INVISIBLE
    }
}

fun View.getVisible() = visibility == View.VISIBLE

fun View.toggle() {
    setVisible(!getVisible())
}

fun View.show() = setVisible(true)

fun View.hide() = setVisible(false)

fun ImageView.load(url: String?, placeholder: Boolean = true,
                   block: RequestCreator.() -> RequestCreator = { this }) {
    val rc = Picasso.get().load(if (url.isNullOrEmpty()) {
        ColorManager.getPhotoStub()
    } else {
        url
    })
    if (placeholder) {
        rc.placeholder(ColorDrawable(ContextCompat.getColor(context, R.color.placeholder)))
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
            .load(if (url.isNullOrEmpty()) {
                ColorManager.getPhotoStub()
            } else {
                url
            })
            .transform(RoundedTransformation())
            .placeholder(R.drawable.placeholder_rounded)
            .error(R.drawable.placeholder_rounded)

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

fun TextView.lower() {
    text = text.toString().toLowerCase()
}

fun EditText.asText() = text.toString()

fun EditText.clear() {
    setText("")
}

fun View.fadeOut(duration: Long, onEnd: () -> Unit = {}) {
    ObjectAnimator.ofPropertyValuesHolder(this,
            PropertyValuesHolder.ofFloat(View.ALPHA, 1f, 0f)
    ).apply {
        this.duration = duration
        addListener(object : StubAnimatorListener() {
            override fun onAnimationEnd(animation: Animator?) {
                onEnd()
            }

            override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                onEnd()
            }
        })
        start()
    }
}

fun View.fadeIn(duration: Long, onEnd: () -> Unit = {}) {
    ObjectAnimator.ofPropertyValuesHolder(this,
            PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f)
    ).apply {
        this.duration = duration
        addListener(object : StubAnimatorListener() {
            override fun onAnimationEnd(animation: Animator?) {
                onEnd()
            }

            override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                onEnd()
            }
        })
        start()
    }
}

open class StubAnimatorListener : Animator.AnimatorListener {
    override fun onAnimationRepeat(animation: Animator?) {

    }

    override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {

    }

    override fun onAnimationEnd(animation: Animator?) {

    }

    override fun onAnimationCancel(animation: Animator?) {

    }

    override fun onAnimationStart(animation: Animator?) {

    }
}