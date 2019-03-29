package com.twoeightnine.root.xvii.chats.messages.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.twoeightnine.root.xvii.chats.messages.starred.StarredMessagesViewModel
import com.twoeightnine.root.xvii.model.Message2
import com.twoeightnine.root.xvii.model.WrappedLiveData
import com.twoeightnine.root.xvii.model.WrappedMutableLiveData
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.network.ApiService
import javax.inject.Inject

abstract class BaseMessagesViewModel(protected val api: ApiService) : ViewModel() {

    protected val messagesLiveData = WrappedMutableLiveData<ArrayList<Message2>>()

    fun getMessages() = messagesLiveData as WrappedLiveData<ArrayList<Message2>>

    abstract fun loadMessages(offset: Int = 0)

    protected fun onMessagesLoaded(messages: ArrayList<Message2>, offset: Int = 0) {
        val existing = if (offset == 0) {
            arrayListOf()
        } else {
            messagesLiveData.value?.data ?: arrayListOf()
        }

        messagesLiveData.value = Wrapper(existing.also { it.addAll(messages) })
    }

    protected fun onErrorOccurred(error: String) {
        messagesLiveData.value = Wrapper(error = error)
    }

    class Factory @Inject constructor(
            private val api: ApiService
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>) = when(modelClass) {
            StarredMessagesViewModel::class.java -> StarredMessagesViewModel(api) as T

            else -> throw IllegalArgumentException("Unknown ViewModel class $modelClass")
        }
    }
}