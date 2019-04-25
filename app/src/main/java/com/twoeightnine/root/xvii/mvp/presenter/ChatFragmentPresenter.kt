package com.twoeightnine.root.xvii.mvp.presenter

import android.content.Context
import android.os.Environment
import android.text.Html
import android.text.TextUtils
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.background.longpoll.models.events.*
import com.twoeightnine.root.xvii.lg.Lg
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.managers.Session
import com.twoeightnine.root.xvii.model.Message
import com.twoeightnine.root.xvii.model.UploadServer
import com.twoeightnine.root.xvii.model.Uploaded
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.model.attachments.Attachment
import com.twoeightnine.root.xvii.model.attachments.Doc
import com.twoeightnine.root.xvii.model.attachments.Sticker
import com.twoeightnine.root.xvii.mvp.BasePresenter
import com.twoeightnine.root.xvii.mvp.view.ChatFragmentView
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.network.response.BaseResponse
import com.twoeightnine.root.xvii.utils.*
import com.twoeightnine.root.xvii.utils.crypto.CryptoUtil
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ChatFragmentPresenter(api: ApiService) : BasePresenter<ChatFragmentView>(api) {

    private val count = 200
    private val keyExTime = 10L

    private val messages: MutableList<Message> = mutableListOf()
    private val users: HashMap<Int, User> = hashMapOf()
    private var isRegistered = false
    private var timeUpSubscription: Disposable? = null
    private var longPollDisposable: Disposable? = null

    lateinit var crypto: CryptoUtil

    var peerId: Int = 0
    var isShown = true
    var isEncrypted = false

    @Inject
    lateinit var utils: ApiUtils

    init {
        subscribe()
        App.appComponent?.inject(this)
    }

    fun subscribe() {
        if (!isRegistered) {
            longPollDisposable = EventBus.subscribeLongPollEventReceived(::onUpdate)
            isRegistered = true
        }
    }

    fun initCrypto() {
        crypto = CryptoUtil(Session.uid, peerId)
        isEncrypted = false
    }

    fun loadHistory(offset: Int = 0, withClear: Boolean = false) {
        view?.showLoading()
        if (offset == 0) {
            messages.clear()
        }
        api.getHistory(count, offset, peerId)
                .compose(applySchedulers())
                .subscribeSmart({ response ->
                    val history = response.items
                    loadUsers(history, withClear)
                }, { error ->
                    view?.showError(error)
                })
    }

    fun loadCachedHistory() {
        view?.showLoading()
        CacheHelper.getMessagesAsync(peerId) { loadUsers(it, cache = true) }
    }

    fun getSaved() = messages

    private fun loadUsers(history: MutableList<Message>, withClear: Boolean = false, cache: Boolean = false) {
        val userIds = getAllIds(history)
        api.getUsers(userIds.joinToString(separator = ","), User.FIELDS)
                .subscribeSmart({ response ->
                    response.map { users.put(it.id, it) }
                    insertUsers(history, withClear, cache)
                    if (peerId.matchesUserId()) {
                        val user = response.find { it.id == peerId }

                        user?.let { view?.onChangeOnline(it.isOnline, it.lastSeen?.time ?: 0) }
                    }
                }, { error ->
                    view?.showError(error)
                })

    }

    private fun insertUsers(history: MutableList<Message>, withClear: Boolean, cache: Boolean = false) {
        history.map { setMessageTitles(users, it, 0) }
                .toMutableList()
        if (withClear) {
            view?.onHistoryClear()
        }
        messages.addAll(0, history)
        CacheHelper.saveMessagesAsync(history)
        view?.onHistoryLoaded(history)
        if (cache) {
            view?.onCacheRestored()
        }
        if (Prefs.markAsRead && history.size > 0) {
            markAsRead(history[0].id)
        }
    }

    fun send(text: String, fwdMessages: String = "", attachments: String = "") {
        val maxLen = 1500
        var forLater = ""
        var message = if (text.length > maxLen) {
            forLater = text.substring(maxLen, text.length)
            text.substring(0, maxLen)
        } else {
            text
        }
        message = if (isEncrypted && message.isNotEmpty()) crypto.encrypt(message) else message
        val flowable: Flowable<BaseResponse<Int>>
        if (peerId.matchesChatId()) {
            flowable = api
                    .sendChat(
                            peerId.asChatId(),
                            message,
                            fwdMessages,
                            attachments,
                            0, null, null
                    )
        } else {
            flowable = api
                    .send(
                            peerId,
                            message,
                            fwdMessages,
                            attachments,
                            0, null, null
                    )
        }
        flowable.subscribeSmart({
            if (Prefs.beOffline) {
                utils.setOffline()
            }
            view?.onAttachmentsSent()
            if (forLater.isNotEmpty()) {
                send(forLater)
            }
        }, { error ->
            Lg.i("send message error: $error")
            view?.showError(error)
            view?.onSentError(text)
            if (Prefs.beOffline) {
                utils.setOffline()
            }
        })
    }

    fun deleteMessages(mids: MutableList<Int>, forAll: Boolean, quiet: Boolean = false) {
        api.deleteMessages(mids.joinToString(separator = ","), if (forAll) 1 else 0)
                .subscribeSmart({
                    view?.onMessagesDeleted(mids)
                }, {
                    if (!quiet) {
                        view?.showError(it)
                    }
                })
    }

    fun editMessage(mid: Int, text: String) {
        api.editMessage(peerId, text, mid)
                .subscribeSmart({
                    if (it == 1) {
                        view?.onMessageEdited(mid, text)
                    }
                }, {
                    view?.showError(it)
                })
    }

    fun sendSticker(sticker: Sticker) {
        val flowable: Flowable<BaseResponse<Int>>
        if (peerId.matchesChatId()) {
            flowable = api
                    .sendChat(
                            peerId.asChatId(),
                            "",
                            null, null, sticker.id, null, null
                    )
        } else {
            flowable = api
                    .send(
                            peerId,
                            "",
                            null, null, sticker.id, null, null
                    )
        }
        flowable.subscribeSmart({
            if (Prefs.beOffline) {
                utils.setOffline()
            }
        }, { error ->
            Lg.i("send sticker error: $error")
            if (error.contains("this sticker")) {
                attachStickerAsPic(sticker)
            } else {
                view?.showError(error)
            }
            if (Prefs.beOffline) {
                utils.setOffline()
            }
        })
    }

    private fun attachStickerAsPic(sticker: Sticker) {
        val fileName = "stick${sticker.id}.png"
        downloadFile(
                App.context,
                sticker.photo256,
                fileName,
                DownloadFileAsyncTask.STI,
                {
                    val file = Environment.getExternalStoragePublicDirectory(DownloadFileAsyncTask.STICKER_PATH)
                    val chosenPhoto = File(file, fileName).absolutePath
                    attachPhoto(chosenPhoto, true)

                }, false, true)
    }

    fun setTyping() {
        if (Prefs.showTyping) {
            utils.setActivity(peerId)
        }
    }

    fun setAudioMessaging() {
        utils.setActivity(peerId, ApiUtils.ACTIVITY_VOICE)
    }

    fun markAsRead(mid: Int) {
        utils.markAsRead("$mid")
    }

    fun markAsImportant(mids: MutableList<Int>, important: Int) {
        api.markMessagesAsImportant(mids.joinToString(separator = ","), important)
                .subscribeSmart({}, {})
    }

    fun attachPhoto(path: String, isSticker: Boolean = false, context: Context? = null) {
        if (isEncrypted && context != null) {
            crypto.encryptFileAsync(context, path) {
                getDocUploadServer(path, it)
            }
        } else {
            getPhotoUploadServer(path, isSticker)
        }
    }

    private fun getPhotoUploadServer(path: String, isSticker: Boolean = false) {
        api.getPhotoUploadServer()
                .subscribeSmart({
                    uploadPhoto(path, it, isSticker)
                }, { error ->
                    view?.showError(error)
                    Lg.wtf("getting ploading server error: $error")
                })
    }

    private fun uploadPhoto(path: String, uploadServer: UploadServer, isSticker: Boolean = false) {
        val file = File(path)
        val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
        val body = MultipartBody.Part.createFormData("photo", file.name, requestFile)
        api.uploadPhoto(uploadServer.uploadUrl ?: "", body)
                .compose(applySchedulers())
                .subscribe({
                    savePhoto(it, path, isSticker)
                }, { error ->
                    val message = error.message ?: "null"
                    Lg.wtf("uploading photo error: $message")
                    view?.showError(message)
                })
    }

    private fun savePhoto(uploaded: Uploaded, path: String, isSticker: Boolean = false) {
        api.saveMessagePhoto(
                uploaded.photo ?: "",
                uploaded.hash ?: "",
                uploaded.server
        )
                .subscribeSmart({
                    val att = Attachment(it[0])
//                    attachUtils.add(att)
                    if (isSticker) {
                        send("")
                    }
                    view?.onPhotoUploaded(path, att)
                }, { error ->
                    view?.showError(error)
                    Lg.wtf("save uploaded photo error: $error")
                })
    }

    fun attachVoice(path: String) {
        getVoiceUploadServer(path)
    }

    private fun getVoiceUploadServer(path: String) {
        api.getDocUploadServer("audio_message")
                .subscribeSmart({
                    uploadVoice(path, it.uploadUrl!!)
                }, {
                    Lg.wtf("getting upload server error: $it")
                    view?.showError(it)
                })
    }

    private fun uploadVoice(path: String, url: String) {
        val file = File(path)
        val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

        api.uploadDoc(url, body)
                .compose(applySchedulers())
                .subscribe({ response ->
                    saveVoice(path, response.file!!)
                }, {
                    Lg.wtf("uploading error: $it")
                    view?.showError(it.message ?: "null")
                })
    }

    private fun saveVoice(path: String, file: String) {
        api.saveDoc(file)
                .subscribeSmart({ response ->
                    if (response.size > 0) {
//                        attachUtils.add(Attachment(response[0]))
                        view?.onVoiceUploaded(path)
                        send("", attachments = response[0].getId())
                    }
                }, { error ->
                    Lg.wtf("saving voice error: $error")
                    view?.showError(error)
                })
    }

    private fun getDocUploadServer(path: String, fileName: String) {
        api.getDocUploadServer("doc")
                .subscribeSmart({
                    uploadDoc(path, it.uploadUrl ?: "", fileName)
                }, { error ->
                    Lg.wtf("getting upload server doc error: $error")
                    view?.showError(error)
                })
    }

    private fun uploadDoc(path: String, url: String, fileName: String) {
        val file = File(fileName)
        val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

        api.uploadDoc(url, body)
                .compose(com.twoeightnine.root.xvii.utils.applySchedulers())
                .subscribe({
                    saveDoc(path, it.file!!)
                }, { error ->
                    Lg.wtf("uploading doc error: $error")
                    view?.showError(error.message ?: "null on uploading")
                })
    }

    private fun saveDoc(path: String, file: String) {
        api.saveDoc(file)
                .subscribeSmart({
                    val att = Attachment(it[0])
//                    attachUtils.add(att)
                    view?.onPhotoUploaded(path, att)
                }, { error ->
                    Lg.wtf("saving doc error: $error")
                    view?.showError(error)
                })
    }

    private fun downloadDoc(context: Context, doc: Doc, callback: (String) -> Unit) {
        if (doc.url == null) return

        val dir = context.cacheDir
        val file = File(dir, doc.title ?: getNameFromUrl(doc.url))
        val fileName = file.absolutePath
        if (File(fileName).exists()) {
            callback.invoke(fileName)
            return
        }
        utils.downloadFile(doc.url, fileName, callback)
    }

    fun decryptDoc(context: Context, doc: Doc, callback: (String) -> Unit) {
        downloadDoc(context, doc) {
            crypto.decryptFileAsync(context, it, callback)
        }
    }

    private fun getAllIds(messes: MutableList<Message>): MutableList<Int> {
        val ids = HashSet<Int>()
        ids.add(Session.uid)
        for (i in messes.indices) {
            if (!users.containsKey(messes[i].userId)) {
                ids.add(messes[i].userId)
            }
            if (messes[i].fwdMessages != null) {
                ids.addAll(getAllIds(messes[i].fwdMessages!!))
            }
        }
        return ids.toMutableList()
    }


    fun startKeyExchange() {
        timeUpSubscription = Flowable.just(true)
                .delay(keyExTime, TimeUnit.SECONDS)
                .observeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .map {
                    Lg.i("waiting state was reset")
                    crypto.isWaiting = false
                    view?.onKeyExchangeFailed()
                }
                .subscribe()
        view?.onKeyGenerating()
        crypto.isWaiting = true
        isEncrypted = false
        crypto.startKeyExchange {
            view?.onKeySent()
            send(it)
        }
    }

    fun supportKeyExchange(key: String) {
        val ownKey = crypto.supportKeyExchange(key)
        isEncrypted = false
        send(ownKey)
        view?.onKeysExchanged()
    }

    fun finishKeyExchange(key: String) {
        timeUpSubscription?.dispose()
        crypto.finishKeyExchange(key)
        view?.onKeysExchanged()
        crypto.printKey()
        crypto.isWaiting = false
    }

    fun setDefaultKey() {
        crypto.resetKeys()
        crypto.printKeys()
    }

    fun setUserKey(key: String) {
        crypto.setUserKey(key)
        crypto.printKeys()
    }

    private fun onUpdate(event: BaseLongPollEvent) {
        when (event) {

            is NewMessageEvent -> {
                if (peerId == event.peerId) {
                    if (!event.isOut()) {
                        view?.onHideTyping()
                        view?.onChangeOnline(true, time())
                    }
                    if (TextUtils.isEmpty(event.text) || event.hasMedia()) {
                        api.getMessageById("${event.id}")
                                .subscribeSmart({ response ->
                                    val message = setMessageTitles(users, response.items[0], 0)
                                    CacheHelper.saveMessageAsync(message)
                                    view?.onMessageAdded(message)
                                    messages.add(0, message)
                                    if (Prefs.markAsRead && isShown) {
                                        markAsRead(message.id)
                                    }
                                }, { error ->
                                    Lg.wtf("new message error: $error")
                                })
                    } else {
                        if (event.text.contains("KeyEx{")) {
                            deleteMessages(mutableListOf(event.id), false, true)
                            if (!event.isOut()) {
                                view?.onKeyReceived(Html.fromHtml(event.text).toString(), crypto.isWaiting)
                            }
                            return
                        }
                        val message = getMessageFromLongPollFull(event, users, isShown)
                        CacheHelper.saveMessageAsync(message)
                        view?.onMessageAdded(message)
                        messages.add(0, message)
                        if (Prefs.markAsRead && isShown) {
                            markAsRead(message.id)
                        }
                    }
                }
            }


            is ReadOutgoingEvent -> {
                if (peerId == event.peerId) {
                    view?.onReadOut(event.mid)
                }
            }

            is OfflineEvent -> {
                if (peerId == event.userId) {
                    view?.onChangeOnline(false)
                }
            }

            is OnlineEvent -> {
                if (peerId == event.userId) {
                    view?.onChangeOnline(true)
                }
            }

            is TypingEvent -> {
                if (peerId == event.userId && isShown) {
                    view?.onShowTyping()
                }
            }

            is TypingChatEvent -> {
                if (peerId == event.peerId && isShown) {
                    view?.onShowTyping()
                }
            }

            is RecordingAudioEvent -> {
                if (peerId == event.peerId && isShown) {
                    view?.onShowRecordingVoice()
                }
            }
        }

    }

    fun unsubscribe() {
        longPollDisposable?.dispose()
        longPollDisposable = null
        isRegistered = false
    }

}