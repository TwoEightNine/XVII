/*
 * xvii - messenger for vk
 * Copyright (C) 2021  TwoEightNine
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.twoeightnine.root.xvii.friends.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.twoeightnine.root.xvii.background.longpoll.models.events.NewMessageEvent
import com.twoeightnine.root.xvii.background.longpoll.models.events.OfflineEvent
import com.twoeightnine.root.xvii.background.longpoll.models.events.OnlineEvent
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.model.WrappedLiveData
import com.twoeightnine.root.xvii.model.WrappedMutableLiveData
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.utils.EventBus
import com.twoeightnine.root.xvii.utils.matchesUserId
import com.twoeightnine.root.xvii.utils.subscribeSmart
import javax.inject.Inject

class FriendsViewModel(private val api: ApiService) : ViewModel() {

    init {
        EventBus.subscribeLongPollEventReceived { event ->
            when (event) {
                is OnlineEvent -> changeStatus(true, event.userId, event.timeStamp, event.deviceCode)
                is OfflineEvent -> changeStatus(false, event.userId, event.timeStamp)
                is NewMessageEvent -> {
                    if (!event.isOut() && event.peerId.matchesUserId()) {
                        changeStatus(true, event.peerId, event.timeStamp)
                    }
                }
            }
        }
    }

    private val friendsLiveData = WrappedMutableLiveData<ArrayList<User>>()

    fun getFriends() = friendsLiveData as WrappedLiveData<ArrayList<User>>

    fun loadFriends(offset: Int = 0) {
        api.getFriends(COUNT, offset)
                .subscribeSmart({ friends ->
                    val existing = if (offset == 0) {
                        arrayListOf()
                    } else {
                        friendsLiveData.value?.data ?: arrayListOf()
                    }
                    existing.addAll(friends.items)
                    friendsLiveData.value = Wrapper(existing)
                }, { error ->
                    friendsLiveData.value = Wrapper(error = error)
                })
    }

    private fun changeStatus(isOnline: Boolean, userId: Int, timeStamp: Int, deviceCode: Int = 0) {
        val user = friendsLiveData.value?.data?.find { it.id == userId } ?: return
        user.isOnline = isOnline
        user.lastSeen?.time = timeStamp
        if (deviceCode != 0) {
            user.lastSeen?.platform = deviceCode
        }

        friendsLiveData.value = Wrapper(friendsLiveData.value?.data)
    }

    companion object {
        const val COUNT = 50
    }

    class Factory @Inject constructor(private val api: ApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>) = FriendsViewModel(api) as T
    }
}