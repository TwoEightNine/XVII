package com.twoeightnine.root.xvii.chats.messages.starred

import com.twoeightnine.root.xvii.chats.messages.base.BaseMessagesViewModel
import com.twoeightnine.root.xvii.model.Message2
import com.twoeightnine.root.xvii.model.Wrapper
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

    fun unmarkMessage(message: Message2) {
        api.markMessagesAsImportant("${message.id}", 0)
                .subscribeSmart({ response ->
                    if (response.getOrNull(0) == message.id) {
                        messagesLiveData.value?.data?.remove(message)
                        messagesLiveData.value = Wrapper(messagesLiveData.value?.data)
                    }
                }, ::onErrorOccurred)
    }

    private fun convert(resp: BaseResponse<MessagesResponse>): BaseResponse<ArrayList<Message2>> {
        val messages = arrayListOf<Message2>()
        val response = resp.response
        response?.messages?.items?.forEach { message ->
            messages.add(message)
            message.name = response.getNameForMessage(message)
            message.photo = response.getPhotoForMessage(message)
            message.read = true
        }

        return BaseResponse(messages, resp.error)
    }

    companion object {
        const val COUNT = 200
    }
}