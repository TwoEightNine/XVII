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

package com.twoeightnine.root.xvii.chats.messages.starred

import com.twoeightnine.root.xvii.chats.messages.Interaction
import com.twoeightnine.root.xvii.chats.messages.base.BaseMessagesViewModel
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.model.messages.Message
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.network.response.BaseResponse
import com.twoeightnine.root.xvii.network.response.MessagesResponse
import com.twoeightnine.root.xvii.utils.subscribeSmart

class StarredMessagesViewModel(api: ApiService) : BaseMessagesViewModel(api) {

    override fun loadMessages(offset: Int) {
        api.getStarredMessages(COUNT, offset)
                .map { convert(it) }
                .subscribeSmart({
                    onMessagesLoaded(it, offset)
                }, ::onErrorOccurred)
    }

    fun unmarkMessage(message: Message) {
        api.markMessagesAsImportant("${message.id}", 0)
                .subscribeSmart({ response ->
                    if (response.getOrNull(0) == message.id) {
//                        messagesLiveData.value?.data?.remove(message)
//                        messagesLiveData.value = Wrapper(messagesLiveData.value?.data)
                        val pos = messages.indexOf(message)
                        messages.remove(message)
                        interactionsLiveData.value = Wrapper(Interaction(Interaction.Type.REMOVE, pos))
                    }
                }, ::onErrorOccurred)
    }

    private fun convert(resp: BaseResponse<MessagesResponse>): BaseResponse<ArrayList<Message>> {
        val messages = arrayListOf<Message>()
        val response = resp.response
        response?.messages?.items?.forEach {
            val message = putTitles(it, response)
            message.read = true
            messages.add(message)
        }

        return BaseResponse(messages, resp.error)
    }

    private fun putTitles(message: Message, response: MessagesResponse): Message {
        message.name = response.getNameForMessage(message)
        message.photo = response.getPhotoForMessage(message)
        val fwd = arrayListOf<Message>()
        message.fwdMessages?.forEach {
            fwd.add(putTitles(it, response))
        }
        message.fwdMessages?.clear()
        message.fwdMessages?.addAll(fwd)
        return message
    }

    companion object {
        const val COUNT = 200
    }
}