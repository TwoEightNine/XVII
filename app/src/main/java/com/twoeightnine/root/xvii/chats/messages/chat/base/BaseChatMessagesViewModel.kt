package com.twoeightnine.root.xvii.chats.messages.chat.base

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.background.longpoll.models.events.*
import com.twoeightnine.root.xvii.chats.attachments.stickersemoji.StickersEmojiRepository
import com.twoeightnine.root.xvii.chats.messages.Interaction
import com.twoeightnine.root.xvii.chats.messages.base.BaseMessagesViewModel
import com.twoeightnine.root.xvii.db.AppDb
import com.twoeightnine.root.xvii.lg.Lg
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.model.CanWrite
import com.twoeightnine.root.xvii.model.LastSeen
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.model.attachments.Attachment
import com.twoeightnine.root.xvii.model.attachments.Sticker
import com.twoeightnine.root.xvii.model.attachments.Video
import com.twoeightnine.root.xvii.model.attachments.isWallPost
import com.twoeightnine.root.xvii.model.messages.Message
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.network.response.BaseResponse
import com.twoeightnine.root.xvii.network.response.MessagesHistoryResponse
import com.twoeightnine.root.xvii.scheduled.core.ScheduledMessage
import com.twoeightnine.root.xvii.scheduled.core.SendMessageWorker
import com.twoeightnine.root.xvii.utils.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import javax.inject.Inject
import kotlin.random.Random

abstract class BaseChatMessagesViewModel(api: ApiService) : BaseMessagesViewModel(api) {

    /**
     * Boolean - online flag
     * Int - last seen time
     * Int - device code
     *
     * [LastSeen] doesn't support online flag
     */
    private val lastSeenLiveData = MutableLiveData<Triple<Boolean, Int, Int>>()
    private val canWriteLiveData = MutableLiveData<CanWrite>()
    private val activityLiveData = MutableLiveData<String>()
    private val eventsDisposable = getEventSubscription()

    /**
     * id of last message that was marked as read. to prevent too many requests
     */
    private var lastMarkedAsReadId: Int = 0

    private val repo by lazy {
        StickersEmojiRepository()
    }

    private val members = arrayListOf<User>()

    private val mentionedMembersLiveData = MutableLiveData<List<User>>()

    @Inject
    lateinit var appDb: AppDb

    val mentionedMembers: LiveData<List<User>>
        get() = mentionedMembersLiveData

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

    init {
        App.appComponent?.inject(this)
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

    fun getLastSeen() = lastSeenLiveData as LiveData<Triple<Boolean, Int, Int>>

    fun getCanWrite() = canWriteLiveData as LiveData<CanWrite>

    fun getActivity() = activityLiveData as LiveData<String>

    protected fun setOffline() {
        if (Prefs.beOffline) {
            api.setOffline()
                    .subscribeSmart({}, {})
        }
    }

    fun loadMembers() {
        api.getConversationMembers(peerId)
                .subscribeSmart({ membersResponse ->
                    members.clear()
                    members.addAll(membersResponse.profiles)
                }, { error ->
                    Lg.i("unable to get conv members: $error")
                })
    }

    fun getMatchingMembers(query: String?) {
        if (query == null) return

        val lowerQuery = query.toLowerCase()
        val mentioned = arrayListOf<User>()
        members.forEach { member ->
            if (member.domain?.toLowerCase()?.startsWith(lowerQuery) == true
                    || member.firstName?.toLowerCase()?.startsWith(lowerQuery) == true
                    || member.lastName?.toLowerCase()?.startsWith(lowerQuery) == true) {
                mentioned.add(member)
            }
        }
        if (USER_ONLINE.domain?.startsWith(lowerQuery) == true) {
            mentioned.add(0, USER_ONLINE)
        }
        if (USER_ALL.domain?.startsWith(lowerQuery) == true) {
            mentioned.add(0, USER_ALL)
        }
        mentionedMembersLiveData.value = mentioned
    }

    fun setActivity(type: String = ACTIVITY_TYPING) {
        if (Prefs.showTyping) {
            api.setActivity(peerId, type)
                    .subscribeSmart({}, {})
        }
    }

    fun markAsRead(messageIds: List<Int>) {
        api.markAsRead(messageIds.joinToString(separator = ",") { it.toString() })
                .subscribeSmart({ _ ->
                    messageIds.max()?.also {
                        lastMarkedAsReadId = it
                    }
                }, {})
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
                    text?.also { StatTool.get()?.messageSent(it) }
                }, { error ->
                    lw("send message: $error")
                })
    }

    @SuppressLint("CheckResult")
    fun scheduleMessage(context: Context, text: String,
                        attachments: String? = null, forwardedMessages: String? = null) {
        val scheduledMessage = ScheduledMessage(
                peerId = peerId,
                whenMs = System.currentTimeMillis() + 100000L,
                text = text,
                attachments = attachments,
                forwardedMessages = forwardedMessages
        )
        appDb.scheduledMessagesDao()
                .addScheduledMessage(scheduledMessage)
                .compose(applyCompletableSchedulers())
                .subscribe({
                    appDb.scheduledMessagesDao()
                            .getLastScheduledMessage()
                            .compose(applySingleSchedulers())
                            .subscribe({ messages ->
                                messages.getOrNull(0)?.also { lastScheduledMessage ->
                                    SendMessageWorker.enqueueWorker(context, lastScheduledMessage)
                                }

                            }, { error ->
                                lw("get last scheduled message: $error")
                            })
                }, { error ->
                    lw("add scheduled message: $error")
                })

    }

    fun sendSticker(sticker: Sticker, replyTo: Int? = null) {
        api.sendMessage(peerId, getRandomId(), stickerId = sticker.stickerId, replyTo = replyTo)
                .subscribeSmart({
                    setOffline()
                    StatTool.get()?.stickerSent()
                    repo.setStickerUsed(sticker.stickerId)
                }, { error ->
                    lw("send sticker: $error")
                })
    }

    fun markAsImportant(messageIds: String) {
        api.markMessagesAsImportant(messageIds, important = 1)
                .subscribeSmart({}, ::onErrorOccurred)
    }

    fun unmarkAsImportant(messageIds: String) {
        api.markMessagesAsImportant(messageIds, important = 0)
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

    fun attachVideo(path: String, onAttached: (String, Attachment) -> Unit) {
        api.getVideoUploadServer()
                .subscribeSmart({ uploadServer ->
                    val file = File(path)
                    val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
                    val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                    api.uploadVideo(uploadServer.uploadUrl ?: return@subscribeSmart, body)
                            .compose(applySchedulers())
                            .subscribe({ response ->
                                val video = Video(response.videoId, response.ownerId)
                                onAttached(path, Attachment(video))
                            }, {
                                lw("uploading error: $it")
                                onErrorOccurred(it.message ?: "")
                            })
                }, { error ->
                    lw("getting upload server error: $error")
                    onErrorOccurred(error)
                })
    }

    fun attachDoc(path: String, onAttached: (String, Attachment) -> Unit) {
        api.getDocUploadServer("doc")
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
                                                onAttached(path, Attachment(it[0]))
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

    /**
     * helps with synchronising ui and viewmodel
     * checks [lastMessageId] with id of last stored message in [messages]
     * if not the same invokes interaction to update ui
     */
    fun invalidateMessages(message: Message) {
        val lastMessage = messages.lastOrNull() ?: return

        if (lastMessage.id != message.id || message.read != lastMessage.read) {
            lw("invalidate messages: last was ${message.id}")
            interactionsLiveData.value = Wrapper(Interaction(Interaction.Type.CLEAR))
            interactionsLiveData.value = Wrapper((Interaction(Interaction.Type.ADD, 0, messages)))
        }
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

    protected open fun onMessageReceived(event: BaseMessageEvent) {
        if (!event.isOut()) {
            val deviceCode = lastSeenLiveData.value?.third ?: 0
            lastSeenLiveData.value = Triple(true, event.timeStamp, deviceCode)
            activityLiveData.value = ACTIVITY_NONE
        }
        if (event.text.isEmpty() || event.hasMedia() || !peerId.matchesUserId()) {
            api.getMessageById(event.id.toString())
                    .map { convert(it, notify = false) }
                    .subscribeSmart({
                        val message = it.getOrNull(0) ?: return@subscribeSmart
                        when (event) {
                            is NewMessageEvent -> addNewMessage(message)
                            is EditMessageEvent -> updateMessage(message, overrideUpdateTime = false)
                        }
                    }, { error ->
                        lw("new message error: $error")
                    })
        } else {
            val message = Message(event, ::prepareTextIn)
            when (event) {
                is NewMessageEvent -> addNewMessage(message)
                is EditMessageEvent -> updateMessage(message)
            }
        }
    }

    private fun addNewMessage(message: Message) {
        // check if [message] is not the latest
        if ((messages.lastOrNull()?.id ?: 0) >= message.id) return

        val count = messages.size
        messages.add(message)
        interactionsLiveData.value = Wrapper(Interaction(Interaction.Type.ADD, count, arrayListOf(message)))
        markAsRead(message.id)
    }

    private fun updateMessage(message: Message, overrideUpdateTime: Boolean = true) {
        val pos = messages.indexOfFirst { it.id == message.id }
        if (pos == -1) return

        if (overrideUpdateTime) {
            message.updateTime = time()
        }
        message.read = messages[pos].read
        messages[pos] = message
        interactionsLiveData.value = Wrapper(Interaction(Interaction.Type.UPDATE, pos, arrayListOf(message)))
    }

    private fun deleteMessage(messageId: Int) {
        val pos = messages.indexOfFirst { it.id == messageId }
        if (pos == -1) return

        messages.removeAt(pos)
        interactionsLiveData.value = Wrapper(Interaction(Interaction.Type.REMOVE, pos))
    }

    private fun convert(resp: BaseResponse<MessagesHistoryResponse>, notify: Boolean = true): BaseResponse<ArrayList<Message>> {
        val messages = arrayListOf<Message>()
        val response = resp.response
        response?.items?.forEach {
            val message = putTitles(it, response)
            message.read = response.isMessageRead(message)
            message.action?.apply {
                subject = response.getProfileById(message.fromId)
                memberId?.also { objekt = response.getProfileById(it) }
            }
            try {
                if (it.attachments?.isWallPost() == true) {
                    val wallPost = it.attachments.getOrNull(0)?.wall
                    wallPost?.group = response.getGroupById(-(wallPost?.fromId ?: 0))
                    wallPost?.user = response.getProfileById(wallPost?.fromId ?: 0)
                }
            } catch (e: Exception) {
                Lg.wtf("[messages] error getting group for wall post: ${e.message}")
            }
            val isEmptyMessage = message.text.isEmpty()
                    && message.fwdMessages.isNullOrEmpty()
                    && message.attachments.isNullOrEmpty()
                    && !message.isSystem()
            if (!isEmptyMessage) {
                messages.add(message)
            }
        }

        if (notify) {
            if (peerId.matchesUserId()) {
                response?.getProfileById(peerId)?.also { user ->
                    val lastSeen = user.lastSeen
                    val value = if (lastSeen == null) {
                        Triple(user.isOnline, 0, 0)
                    } else {
                        Triple(user.isOnline, lastSeen.time, lastSeen.platform)
                    }
                    lastSeenLiveData.postValue(value)
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
                lastSeenLiveData.value = Triple(true, event.timeStamp, event.deviceCode)
            }
            is OfflineEvent -> if (event.userId == peerId) {
                val deviceCode = lastSeenLiveData.value?.third ?: 0
                lastSeenLiveData.value = Triple(false, event.timeStamp, deviceCode)
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
            is BaseMessageEvent -> if (event.peerId == peerId) {
                onMessageReceived(event)
            }
            is InstallFlagsEvent -> if (event.peerId == peerId && event.isDeleted) {
                deleteMessage(event.id)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        eventsDisposable?.dispose()
        repo.destroy()
    }

    protected fun getRandomId() = Random.nextInt()

    protected fun lw(s: String) {
        Lg.wtf("[chat] $s")
    }

    companion object {

        val USER_ALL = User(
                domain = "all",
                firstName = "all",
                lastName = ""
        )
        val USER_ONLINE = User(
                domain = "online",
                firstName = "online",
                lastName = ""
        )

        const val COUNT = 50

        const val ACTIVITY_TYPING = "typing"
        const val ACTIVITY_VOICE = "audiomessage"
        const val ACTIVITY_NONE = "none"
    }
}