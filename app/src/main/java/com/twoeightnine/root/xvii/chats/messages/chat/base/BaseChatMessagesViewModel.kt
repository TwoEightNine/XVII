package com.twoeightnine.root.xvii.chats.messages.chat.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.twoeightnine.root.xvii.background.longpoll.models.events.*
import com.twoeightnine.root.xvii.chats.messages.Interaction
import com.twoeightnine.root.xvii.chats.messages.base.BaseMessagesViewModel
import com.twoeightnine.root.xvii.lg.Lg
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.model.CanWrite
import com.twoeightnine.root.xvii.model.LastSeen
import com.twoeightnine.root.xvii.model.Message
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

abstract class BaseChatMessagesViewModel(api: ApiService) : BaseMessagesViewModel(api) {

    /**
     * Boolean - online flag
     * Int - last seen time
     *
     * [LastSeen] doesn't support online flag
     */
    private val lastSeenLiveData = MutableLiveData<Pair<Boolean, Int>>()
    private val canWriteLiveData = MutableLiveData<CanWrite>()
    private val activityLiveData = MutableLiveData<String>()
    private val eventsDisposable = getEventSubscription()

    /**
     * id of last message that was marked as read. to prevent too many requests
     */
    private var lastMarkedAsReadId: Int = 0

    var peerId: Int = 0
        set(value) {
            if (field == 0) {
                field = value
            }
        }

    var isShown = false
        set(value) {
            if (field == value) return

            field = value
            if (field) {
                val message = messages.lastOrNull() ?: return

                if (message.id != lastMarkedAsReadId) {
                    markAsRead(message.id)
                }
            }
        }

    /**
     * prepares outgoing message text before sending or editing
     */
    abstract fun prepareTextOut(text: String?): String

    /**
     * prepares incoming message text before showing
     */
    abstract fun prepareTextIn(text: String): String

    /**
     * attaching photo has different logic
     */
    abstract fun attachPhoto(path: String, onAttached: (String, Attachment) -> Unit)

    fun getLastSeen() = lastSeenLiveData as LiveData<Pair<Boolean, Int>>

    fun getCanWrite() = canWriteLiveData as LiveData<CanWrite>

    fun getActivity() = activityLiveData as LiveData<String>

    protected fun setOffline() {
        if (Prefs.beOffline) {
            api.setOffline()
                    .subscribeSmart({}, {})
        }
    }

    fun setActivity(type: String = ACTIVITY_TYPING) {
        api.setActivity2(peerId, type)
                .subscribeSmart({}, {})
    }

    private fun markAsRead(messageId: Int) {
        if (Prefs.markAsRead && isShown) {
            api.markAsRead("$messageId")
                    .subscribeSmart({
                        lastMarkedAsReadId = messageId
                    }, {})
        }
    }

    fun editMessage(messageId: Int, text: String) {
        api.editMessage(peerId, prepareTextOut(text), messageId)
                .subscribeSmart({}, ::onErrorOccurred)
    }

    fun sendMessage(text: String? = null, attachments: String? = null,
                    replyTo: Int? = null, forwardedMessages: String? = null) {
        // reply with empty message is prohibited. send it as forwarded
        if (text.isNullOrEmpty() && replyTo != null) {
            api.sendMessage(peerId, getRandomId(), prepareTextOut(text), "$replyTo", attachments)
        } else {
            api.sendMessage(peerId, getRandomId(), prepareTextOut(text), forwardedMessages, attachments, replyTo)
        }
                .subscribeSmart({
                    setOffline()
                }, { error ->
                    lw("send message: $error")
                })

    }

    fun sendSticker(sticker: Sticker, replyTo: Int? = null) {
        api.sendMessage(peerId, getRandomId(), stickerId = sticker.id, replyTo = replyTo)
                .subscribeSmart({
                    setOffline()
                }, { error ->
                    lw("send sticker: $error")
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
                    if (offset == 0 && messages.isNotEmpty()) {
                        markAsRead(messages[0].id)
                    }
                }, ::onErrorOccurred)
    }

    private fun readOutgoingMessages() {
        val firstUnreadPos = messages.indexOfFirst { !it.read }
        if (firstUnreadPos == -1) return

        val unreadMessages = messages.subList(firstUnreadPos, messages.size)
        unreadMessages.forEach { it.read = true }
        interactionsLiveData.value = Wrapper(Interaction(Interaction.Type.UPDATE, firstUnreadPos, unreadMessages))
    }

    protected open fun onMessageReceived(event: NewMessageEvent) {
        if (!event.isOut()) {
            lastSeenLiveData.value = Pair(true, event.timeStamp)
            activityLiveData.value = ACTIVITY_NONE
        }
        if (event.text.isEmpty() || event.hasMedia() || !peerId.matchesUserId()) {
            api.getMessageById(event.id.toString())
                    .map { convert(it, notify = false) }
                    .subscribeSmart({
                        addNewMessage(it.getOrNull(0) ?: return@subscribeSmart)
                    }, { error ->
                        lw("new message error: $error")
                    })
        } else {
            addNewMessage(Message(event, ::prepareTextIn))
        }
    }

    private fun addNewMessage(message: Message) {
        interactionsLiveData.value = Wrapper(Interaction(Interaction.Type.ADD, messages.size, arrayListOf(message)))
        messages.add(message)
        markAsRead(message.id)
    }

    private fun convert(resp: BaseResponse<MessagesHistoryResponse>, notify: Boolean = true): BaseResponse<ArrayList<Message>> {
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

        if (notify) {
            if (peerId.matchesUserId()) {
                response?.getProfileById(peerId)?.also { user ->
                    lastSeenLiveData.postValue(Pair(user.isOnline, user.lastSeen?.time ?: 0))
                }
            }
            response?.conversations?.getOrNull(0)?.canWrite?.also { canWrite ->
                canWriteLiveData.postValue(canWrite)
            }
        }
        return BaseResponse(messages, resp.error)
    }

    private fun putTitles(message: Message, response: MessagesHistoryResponse): Message {
        message.name = response.getNameForMessage(message)
        message.photo = response.getPhotoForMessage(message)
        message.text = prepareTextIn(message.text)
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

    private fun getEventSubscription() = EventBus.subscribeLongPollEventReceived { event ->
        when (event) {
            is OnlineEvent -> if (event.userId == peerId) {
                lastSeenLiveData.value = Pair(first = true, second = event.timeStamp)
            }
            is OfflineEvent -> if (event.userId == peerId) {
                lastSeenLiveData.value = Pair(first = false, second = event.timeStamp)
            }
            is ReadOutgoingEvent -> if (event.peerId == peerId) {
                readOutgoingMessages()
            }
            is TypingEvent -> if (event.userId == peerId) {
                activityLiveData.value = ACTIVITY_TYPING
            }
            is TypingChatEvent -> if (event.peerId == peerId) {
                activityLiveData.value = ACTIVITY_TYPING
            }
            is RecordingAudioEvent -> if (event.peerId == peerId) {
                activityLiveData.value = ACTIVITY_VOICE
            }
            is NewMessageEvent -> if (event.peerId == peerId) {
                onMessageReceived(event)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        eventsDisposable?.dispose()
    }

    protected fun getRandomId() = Random.nextInt()

    protected fun lw(s: String) {
        Lg.wtf("[chat] $s")
    }

    companion object {
        const val COUNT = 200

        const val ACTIVITY_TYPING = "typing"
        const val ACTIVITY_VOICE = "audiomessage"
        const val ACTIVITY_NONE = "none"
    }
}