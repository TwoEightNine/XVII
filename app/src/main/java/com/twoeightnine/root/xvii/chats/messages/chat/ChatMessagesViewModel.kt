package com.twoeightnine.root.xvii.chats.messages.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.twoeightnine.root.xvii.background.longpoll.models.events.OfflineEvent
import com.twoeightnine.root.xvii.background.longpoll.models.events.OnlineEvent
import com.twoeightnine.root.xvii.background.longpoll.models.events.ReadOutgoingEvent
import com.twoeightnine.root.xvii.chats.messages.base.BaseMessagesViewModel
import com.twoeightnine.root.xvii.lg.Lg
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.model.CanWrite
import com.twoeightnine.root.xvii.model.LastSeen
import com.twoeightnine.root.xvii.model.Message2
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.model.attachments.Attachment
import com.twoeightnine.root.xvii.model.attachments.Sticker
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.network.response.BaseResponse
import com.twoeightnine.root.xvii.network.response.MessagesHistoryResponse
import com.twoeightnine.root.xvii.utils.EventBus
import com.twoeightnine.root.xvii.utils.applySchedulers
import com.twoeightnine.root.xvii.utils.matchesUserId
import com.twoeightnine.root.xvii.utils.subscribeSmart
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import kotlin.random.Random

class ChatMessagesViewModel(api: ApiService) : BaseMessagesViewModel(api) {

    /**
     * Boolean - online flag
     * Int - last seen time
     *
     * [LastSeen] doesn't support online flag
     */
    private val lastSeenLiveData = MutableLiveData<Pair<Boolean, Int>>()
    private val canWriteLiveData = MutableLiveData<CanWrite>()

    var peerId: Int = 0
        set(value) {
            if (field == 0) {
                field = value
            }
        }

    init {
        EventBus.subscribeLongPollEventReceived { event ->
            when(event) {
                is OnlineEvent -> if (event.userId == peerId) {
                    lastSeenLiveData.value = Pair(first = true, second = event.timeStamp)
                }
                is OfflineEvent -> if (event.userId == peerId) {
                    lastSeenLiveData.value = Pair(first = true, second = event.timeStamp)
                }
                is ReadOutgoingEvent -> if (event.peerId == peerId) {
                    readOutgoingMessages()
                }
            }
        }
    }

    fun getLastSeen() = lastSeenLiveData as LiveData<Pair<Boolean, Int>>

    fun getCanWrite() = canWriteLiveData as LiveData<CanWrite>

    fun setOffline() {
        if (Prefs.beOffline) {
            api.setOffline()
                    .subscribeSmart({}, {})
        }
    }

    fun setActivity(type: String = ACTIVITY_TYPING) {
        api.setActivity2(peerId, type)
                .subscribeSmart({}, {})
    }

    fun markAsRead(messageIds: String) {
        if (Prefs.markAsRead) {
            api.markAsRead(messageIds)
                    .subscribeSmart({}, {})
        }
    }

    fun editMessage(messageId: Int, text: String) {
        api.editMessage(peerId, text, messageId)
                .subscribeSmart({}, ::onErrorOccurred)
    }

    fun sendMessage(text: String? = null, attachments: String? = null,
                    replyTo: Int? = null, forwardedMessages: String? = null) {
        api.sendMessage(peerId, getRandomId(), text, forwardedMessages, attachments, replyTo)
                .subscribeSmart({
                    setOffline()
                }, { error ->

                })
    }

    fun sendSticker(sticker: Sticker, replyTo: Int? = null) {
        api.sendMessage(peerId, getRandomId(), stickerId = sticker.id, replyTo = replyTo)
                .subscribeSmart({
                    setOffline()
                }, { error ->

                })
    }

    fun markAsImportant(messageIds: String) {
        api.markMessagesAsImportant(messageIds, important = 1)
                .subscribeSmart({}, ::onErrorOccurred)
    }

    fun deleteMessages(messageIds: String, forAll: Boolean) {
        api.deleteMessages(messageIds, if (forAll) 1 else 0)
                .subscribeSmart({}, ::onErrorOccurred)
    }

    fun attachPhoto(path: String, onAttached: (String, Attachment) -> Unit) {
        api.getPhotoUploadServer()
                .subscribeSmart({ uploadServer ->
                    val file = File(path)
                    val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
                    val body = MultipartBody.Part.createFormData("photo", file.name, requestFile)
                    api.uploadPhoto(uploadServer.uploadUrl ?: "", body)
                            .compose(applySchedulers())
                            .subscribe({ uploaded ->
                                api.saveMessagePhoto(
                                        uploaded.photo ?: "",
                                        uploaded.hash ?: "",
                                        uploaded.server
                                )
                                        .subscribeSmart({
                                            onAttached(path, Attachment(it[0]))
                                        }, { error ->
                                            onErrorOccurred(error)
                                            lw("save uploaded photo error: $error")
                                        })
                            }, { error ->
                                val message = error.message ?: "null"
                                lw("uploading photo error: $message")
                                onErrorOccurred(message)
                            })
                }, { error ->
                    onErrorOccurred(error)
                    lw("getting ploading server error: $error")
                })
    }

    fun attachVoice(path: String, onAttached: (String) -> Unit) {
        api.getDocUploadServer("audio_message")
                .subscribeSmart({ uploadServer ->
                    val file = File(path)
                    val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
                    val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                    api.uploadDoc(uploadServer.uploadUrl ?: return@subscribeSmart, body)
                            .compose(applySchedulers())
                            .subscribe({ response ->
                                api.saveDoc(response.file ?: return@subscribe)
                                        .subscribeSmart({
                                            if (it.size > 0) {
                                                onAttached(path)
                                                sendMessage(attachments = it[0].getId())
                                            }
                                        }, { error ->
                                            lw("saving voice error: $error")
                                            onErrorOccurred(error)
                                        })
                            }, {
                                lw("uploading error: $it")
                                onErrorOccurred(it.message ?: "")
                            })
                }, { error ->
                    lw("getting upload server error: $error")
                    onErrorOccurred(error)
                })
    }

    override fun loadMessages(offset: Int) {
        api.getMessages(peerId, COUNT, offset)
                .map { convert(it) }
                .subscribeSmart({ messages ->
                    onMessagesLoaded(messages, offset)
                    if (offset == 0) {
                        markAsRead(messages[0].id.toString())
                    }
                }, ::onErrorOccurred)
    }

    private fun readOutgoingMessages() {
        messagesLiveData.value?.data?.forEach {
            it.read = true
        }
        messagesLiveData.value = Wrapper(messagesLiveData.value?.data)
    }

    private fun convert(resp: BaseResponse<MessagesHistoryResponse>): BaseResponse<ArrayList<Message2>> {
        val messages = arrayListOf<Message2>()
        val response = resp.response
        response?.items?.forEach {
            val message = putTitles(it, response)
            message.read = response.isMessageRead(message)
            messages.add(message)
        }

        if (peerId.matchesUserId()) {
            response?.getProfileById(peerId)?.also { user ->
                lastSeenLiveData.postValue(Pair(user.isOnline, user.lastSeen?.time ?: 0))
            }
        }
        canWriteLiveData.postValue(response?.conversations?.getOrNull(0)?.canWrite)
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

    private fun getRandomId() = Random.nextInt()

    private fun lw(s: String) {
        Lg.wtf("[chat] $s")
    }

    companion object {
        const val COUNT = 200

        const val ACTIVITY_TYPING = "typing"
        const val ACTIVITY_VOICE = "audiomessage"
    }
}