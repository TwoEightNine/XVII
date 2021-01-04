package com.twoeightnine.root.xvii.base

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.utils.AsyncUtils

open class BaseViewModel : ViewModel() {

    val error: LiveData<String>
        get() = errorLiveData

    private val multiCancellable = AsyncUtils.MultiCancellable()

    private val errorLiveData = MutableLiveData<String>()

    protected val applicationContext: Context
        get() = App.context

    protected fun <T> onIoThread(callable: () -> T, onSuccess: (T) -> Unit) {
        AsyncUtils.onIoThread(callable, ::onErrorOccurred, onSuccess).watch()
    }

    protected fun <T> onIoThreadNullable(callable: () -> T?, onSuccess: (T?) -> Unit) {
        AsyncUtils.onIoThreadNullable(callable, ::onErrorOccurred, onSuccess).watch()
    }

    protected fun onDelayed(millis: Long, callable: () -> Unit) {
        AsyncUtils.runDelayed(millis, callable).watch()
    }

    protected open fun onErrorOccurred(throwable: Throwable?) {
        errorLiveData.value = throwable?.message
    }

    private fun AsyncUtils.Cancellable.watch() {
        multiCancellable.add(this)
    }

    override fun onCleared() {
        super.onCleared()
        multiCancellable.cancel()
    }
}