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

package com.twoeightnine.root.xvii.chats.messages.base

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.messages.Interaction
import com.twoeightnine.root.xvii.chats.messages.chat.secret.SecretChatViewModel
import com.twoeightnine.root.xvii.chats.messages.chat.usual.ChatMessagesViewModel
import com.twoeightnine.root.xvii.chats.messages.deepforwarded.DeepForwardedViewModel
import com.twoeightnine.root.xvii.chats.messages.starred.StarredMessagesViewModel
import com.twoeightnine.root.xvii.chats.tools.ChatStorage
import com.twoeightnine.root.xvii.model.WrappedLiveData
import com.twoeightnine.root.xvii.model.WrappedMutableLiveData
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.model.attachments.Video
import com.twoeightnine.root.xvii.model.messages.Message
import com.twoeightnine.root.xvii.model.messages.WrappedMessage
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.utils.subscribeSmart
import javax.inject.Inject

abstract class BaseMessagesViewModel(protected val api: ApiService) : ViewModel() {

    /**
     * stored in natural ui order: eldest first
     */
    protected val messages = arrayListOf<WrappedMessage>()

    protected val interactionsLiveData = WrappedMutableLiveData<Interaction>()

    fun getInteraction() = interactionsLiveData as WrappedLiveData<Interaction>

    abstract fun loadMessages(offset: Int = 0)

    fun getStoredMessages() = ArrayList(messages)

    fun loadVideo(
            context: Context,
            video: Video,
            onLoaded: (String) -> Unit,
            onError: (String) -> Unit
    ) {
        api.getVideos(
                video.videoId,
                video.accessKey ?: "",
                1, 0
        )
                .subscribeSmart({ response ->
                    if (response.items.size > 0 && response.items[0].player != null) {
                        onLoaded(response.items[0].player ?: "")
                    } else {
                        onError(context.getString(R.string.not_playable_video))
                    }
                }, onError)
    }

    protected fun onMessagesLoaded(items: ArrayList<Message>, offset: Int = 0) {
        val newMessages = items.reversed().map(::WrappedMessage)
        if (offset == 0) {
            messages.clear()
            interactionsLiveData.value = Wrapper(Interaction(Interaction.Type.CLEAR))
        }
        messages.addAll(0, newMessages)
        interactionsLiveData.value = Wrapper(Interaction(Interaction.Type.ADD, 0, newMessages))

    }

    protected fun onErrorOccurred(error: String) {
        interactionsLiveData.value = Wrapper(error = error)
    }

    class Factory @Inject constructor(
            private val api: ApiService,
            private val context: Context,
            private val chatStorage: ChatStorage
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>) = when (modelClass) {
            StarredMessagesViewModel::class.java -> StarredMessagesViewModel(api) as T
            DeepForwardedViewModel::class.java -> DeepForwardedViewModel(api) as T
            ChatMessagesViewModel::class.java -> ChatMessagesViewModel(api, chatStorage) as T
            SecretChatViewModel::class.java -> SecretChatViewModel(api, context) as T

            else -> throw IllegalArgumentException("Unknown ViewModel class $modelClass")
        }
    }
}