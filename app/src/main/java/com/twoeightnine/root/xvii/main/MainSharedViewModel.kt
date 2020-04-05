package com.twoeightnine.root.xvii.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainSharedViewModel : ViewModel() {

    private val bottomInsetLiveData = MutableLiveData<Int>()
    private val topInsetLiveData = MutableLiveData<Int>()

    val bottomInset: LiveData<Int>
        get() = bottomInsetLiveData

    val topInset: LiveData<Int>
        get() = topInsetLiveData

    fun updateBottomInset(inset: Int) {
        bottomInsetLiveData.value = inset
    }

    fun updateTopInset(inset: Int) {
        topInsetLiveData.value = inset
    }

}