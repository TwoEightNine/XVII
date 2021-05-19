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

package com.twoeightnine.root.xvii.chats.messages.deepforwarded

import com.twoeightnine.root.xvii.chats.messages.base.BaseMessagesViewModel
import com.twoeightnine.root.xvii.model.messages.Message
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.network.response.BaseResponse
import com.twoeightnine.root.xvii.network.response.MessagesHistoryResponse
import com.twoeightnine.root.xvii.utils.subscribeSmart

class DeepForwardedViewModel(api: ApiService) : BaseMessagesViewModel(api) {

    override fun loadMessages(offset: Int) {}

    fun loadMessage(messageId: Int) {
        api.getMessageById(messageId.toString())
                .map(::convert)
                .subscribeSmart({ response ->
                    onMessagesLoaded(response)
                }, ::onErrorOccurred)
    }

    private fun convert(resp: BaseResponse<MessagesHistoryResponse>): BaseResponse<ArrayList<Message>> {
        val messages = arrayListOf<Message>()
        val response = resp.response
        response?.items?.forEach {
            val message = putTitles(it, response)
            message.read = response.isMessageRead(message)
            val isEmptyMessage = message.text.isEmpty()
                    && message.fwdMessages.isNullOrEmpty()
                    && message.attachments.isNullOrEmpty()
            if (!isEmptyMessage) {
                messages.add(message)
            }
        }
        return BaseResponse(messages, resp.error)
    }

    private fun putTitles(message: Message, response: MessagesHistoryResponse): Message {
        message.name = response.getNameForMessage(message)
        message.photo = response.getPhotoForMessage(message)
        val fwd = arrayListOf<Message>()
        message.fwdMessages?.forEach {
            fwd.add(putTitles(it, response))
        }
        message.replyMessage?.also {
            message.replyMessage = putTitles(it, response)
        }
        message.fwdMessages?.clear()
        message.fwdMessages?.addAll(fwd)
        return message
    }
}