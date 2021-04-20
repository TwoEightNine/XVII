package com.twoeightnine.root.xvii.utils

import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import rx.Subscription
import java.util.concurrent.TimeUnit

object AsyncUtils {

    private val defaultError = { throwable: Throwable -> }

    fun <T> onIoThread(
            callable: () -> T,
            onError: (Throwable) -> Unit = defaultError,
            onSuccess: (T) -> Unit = {}
    ): Cancellable =
            Single.fromCallable(callable)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(onSuccess, onError)
                    .toCancellable()

    fun <T> onIoThreadNullable(
            callable: () -> T?,
            onError: (Throwable) -> Unit = defaultError,
            onSuccess: (T?) -> Unit = {}
    ): Cancellable =
            Single.fromCallable { Emittable(callable()) }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ onSuccess(it.something) }, onError)
                    .toCancellable()

    fun runDelayed(millis: Long, onSuccess: () -> Unit): Cancellable =
            Single.timer(millis, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { _: Long -> onSuccess() }
                    .toCancellable()

    private fun Disposable.toCancellable(): Cancellable = DisposableCancellable(this)

    private fun Subscription.toCancellable(): Cancellable = SubscriptionCancellable(this)

    interface Cancellable {

        fun cancel()

        fun isCancelled(): Boolean
    }

    class DisposableCancellable(private val disposable: Disposable) : Cancellable {

        override fun cancel() = disposable.dispose()

        override fun isCancelled() = disposable.isDisposed
    }

    class SubscriptionCancellable(private val subscription: Subscription) : Cancellable {

        override fun cancel() = subscription.unsubscribe()

        override fun isCancelled() = subscription.isUnsubscribed
    }

    class MultiCancellable : Cancellable {

        private val cancellables = arrayListOf<Cancellable>()
        private var isCancelled = false

        fun add(cancellable: Cancellable) {
            if (!isCancelled && !cancellable.isCancelled()) {
                cancellables.add(cancellable)
            }
        }

        override fun cancel() {
            cancellables.forEach { it.cancel() }
        }

        override fun isCancelled(): Boolean = isCancelled
    }

    private data class Emittable<T>(
            val something: T?
    )
}