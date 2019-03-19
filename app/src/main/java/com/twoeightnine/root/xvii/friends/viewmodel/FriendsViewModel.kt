package com.twoeightnine.root.xvii.friends.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.twoeightnine.root.xvii.background.longpoll.models.events.OfflineEvent
import com.twoeightnine.root.xvii.background.longpoll.models.events.OnlineEvent
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.model.WrappedLiveData
import com.twoeightnine.root.xvii.model.WrappedMutableLiveData
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.utils.EventBus
import com.twoeightnine.root.xvii.utils.subscribeSmart
import javax.inject.Inject

class FriendsViewModel(private val api: ApiService) : ViewModel() {

    init {
        EventBus.subscribeLongPollEventReceived { event ->
            when(event) {
                is OnlineEvent -> changeStatus(true, event.userId)
                is OfflineEvent -> changeStatus(false, event.userId)
            }
        }
    }

    private val friendsLiveData = WrappedMutableLiveData<ArrayList<User>>()

    fun getFriends() = friendsLiveData as WrappedLiveData<ArrayList<User>>

    fun loadFriends(offset: Int = 0) {
        api.getFriends(COUNT, offset)
                .subscribeSmart({ friends ->
                    friendsLiveData.value = Wrapper(ArrayList(friends.items))
                }, { error ->
                    friendsLiveData.value = Wrapper(error = error)
                })
    }

    private fun changeStatus(isOnline: Boolean, userId: Int) {
        val user = friendsLiveData.value?.data?.first { it.id == userId } ?: return
        user.isOnline = isOnline

        friendsLiveData.value = Wrapper(friendsLiveData.value?.data)
    }

    companion object {
        const val COUNT = 200
    }

    class Factory @Inject constructor(private val api: ApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>) = FriendsViewModel(api) as T
    }
}