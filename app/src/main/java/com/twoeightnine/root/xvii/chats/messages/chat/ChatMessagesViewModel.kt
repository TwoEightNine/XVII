package com.twoeightnine.root.xvii.chats.messages.chat

import com.twoeightnine.root.xvii.chats.messages.base.BaseMessagesViewModel
import com.twoeightnine.root.xvii.model.Message2
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.network.response.BaseResponse
import com.twoeightnine.root.xvii.network.response.MessagesHistoryResponse
import com.twoeightnine.root.xvii.utils.subscribeSmart

class ChatMessagesViewModel(api: ApiService) : BaseMessagesViewModel(api) {

    var peerId: Int = 0
        set(value) {
            if (field == 0) {
                field = value
            }
        }

    override fun loadMessages(offset: Int) {
        api.getMessages(peerId, COUNT, offset)
                .map { convert(it) }
                .subscribeSmart({ messages ->
                    onMessagesLoaded(messages, offset)
                }, ::onErrorOccurred)
    }

    private fun convert(resp: BaseResponse<MessagesHistoryResponse>): BaseResponse<ArrayList<Message2>> {
        val messages = arrayListOf<Message2>()
        val response = resp.response
        response?.items?.forEach {
            val message = putTitles(it, response)
            message.read = response.isMessageRead(message)
            messages.add(message)
        }

        return BaseResponse(messages, resp.error)
    }

    private fun putTitles(message: Message2, response: MessagesHistoryResponse): Message2 {
        message.name = response.getNameForMessage(message)
        message.photo = response.getPhotoForMessage(message)
        val fwd = arrayListOf<Message2>()
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