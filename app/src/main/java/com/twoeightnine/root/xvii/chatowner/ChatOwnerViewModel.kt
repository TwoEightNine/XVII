package com.twoeightnine.root.xvii.chatowner

import androidx.lifecycle.ViewModel
import com.twoeightnine.root.xvii.chatowner.model.ChatOwner
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.model.WrappedLiveData
import com.twoeightnine.root.xvii.model.WrappedMutableLiveData

class ChatOwnerViewModel : ViewModel() {

    private val chatOwnerLiveData = WrappedMutableLiveData<ChatOwner>()

    val chatOwner: WrappedLiveData<ChatOwner>
        get() = chatOwnerLiveData

    fun <T : ChatOwner> loadChatOwner(peerId: Int, chatOwnerClass: Class<T>) {
        when(chatOwnerClass) {
            User::class.java -> {

            }
        }
    }

}