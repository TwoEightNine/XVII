package com.twoeightnine.root.xvii.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

data class Wrapper<T>(
    val data: T? = null,
    val error: String? = null
)

typealias WrappedLiveData<T> = LiveData<Wrapper<T>>
typealias WrappedMutableLiveData<T> = MutableLiveData<Wrapper<T>>