package com.twoeightnine.root.xvii.chats.messages.chat.secret

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.twoeightnine.root.xvii.background.longpoll.models.events.NewMessageEvent
import com.twoeightnine.root.xvii.chats.messages.chat.base.BaseChatMessagesViewModel
import com.twoeightnine.root.xvii.crypto.CryptoEngine
import com.twoeightnine.root.xvii.lg.Lg
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.model.attachments.Attachment
import com.twoeightnine.root.xvii.model.attachments.Doc
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.utils.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class SecretChatViewModel(
        api: ApiService,
        private val context: Context
) : BaseChatMessagesViewModel(api) {

    private var isExchangeStarted = false

    private val crypto by lazy {
        CryptoEngine(context, peerId)
    }

    private val keysSetLiveData = MutableLiveData<Boolean>()

    fun getKeysSet() = keysSetLiveData as LiveData<Boolean>

    fun isKeyRequired() = crypto.isKeyRequired()

    fun getFingerprint() = crypto.getFingerPrint()

    fun getKeyType() = crypto.keyType

    fun setKey(key: String) {
        crypto.setKey(key)
    }

    fun startExchange() {
        crypto.startExchange {
            l("start exchange")
            Lg.dbg(it)
            sendData(it)
            isExchangeStarted = true
        }
    }

    override fun loadMessages(offset: Int) {
        if (!isKeyRequired()) {
            super.loadMessages(offset)
        } else {
            messagesLiveData.value = Wrapper(arrayListOf())
        }
    }

    override fun onMessageReceived(event: NewMessageEvent) {
        if (event.text.matchesXviiKeyEx()) {
            if (!event.isOut()) {
                if (isExchangeStarted) {
                    l("receive support")
                    Lg.dbg(event.text)
                    crypto.finishExchange(event.text)
                    isExchangeStarted = false
                    keysSetLiveData.value = true
                } else {
                    l("receive exchange")
                    Lg.dbg(event.text)
                    val ownKeys = crypto.supportExchange(event.text)
                    sendData(ownKeys)
                }
            }
            deleteMessages(event.id.toString(), forAll = false)
        } else if (!isKeyRequired()) {
            super.onMessageReceived(event)
        }
    }

    override fun attachPhoto(path: String, onAttached: (String, Attachment) -> Unit) {
        api.getDocUploadServer("doc")
                .subscribeSmart({ uploadServer ->
                    crypto.encryptFile(context, path) { encryptedPath ->
                        val file = File(encryptedPath)
                        val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
                        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                        api.uploadDoc(uploadServer.uploadUrl ?: "", body)
                                .compose(applySchedulers())
                                .subscribe({ uploaded ->
                                    api.saveDoc(uploaded.file ?: return@subscribe)
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
                    }

                }, { error ->
                    onErrorOccurred(error)
                    lw("getting ploading server error: $error")
                })
    }

    fun decryptDoc(doc: Doc, callback: (Boolean, String?) -> Unit) {
        downloadDoc(doc) {
            crypto.decryptFile(context, it, callback)
        }
    }

    private fun sendData(data: String) {
        api.sendMessage(peerId, getRandomId(), data)
                .subscribeSmart({
                    setOffline()
                }, { error ->
                    lw("send message: $error")
                })
    }

    @SuppressLint("CheckResult")
    private fun downloadDoc(doc: Doc, callback: (String) -> Unit) {
        if (doc.url == null) return

        val dir = context.cacheDir
        val file = File(dir, doc.title ?: getNameFromUrl(doc.url))
        val fileName = file.absolutePath
        if (File(fileName).exists()) {
            callback.invoke(fileName)
            return
        }
        api.downloadFile(doc.url)
                .compose(applySchedulers())
                .subscribe({
                    val written = writeResponseBodyToDisk(it, fileName)
                    if (written) {
                        callback(fileName)
                    } else {
                        lw("Error downloading $fileName: not written")
                    }
                }, {
                    it.printStackTrace()
                    val errorMsg = it.message ?: "download file error: null error"
                    lw(errorMsg)
                })
    }

    override fun prepareTextOut(text: String?) = when {
        text.isNullOrEmpty() -> ""
        else -> crypto.encrypt(text)
    }

    override fun prepareTextIn(text: String): String {
        val cipherResult = crypto.decrypt(text)
        return if (cipherResult.verified && cipherResult.bytes != null) {
            String(cipherResult.bytes)
        } else {
            ""
        }
    }

    private fun l(s: String) {
        Lg.i("[secret] $s")
    }
}