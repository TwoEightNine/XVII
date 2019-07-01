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