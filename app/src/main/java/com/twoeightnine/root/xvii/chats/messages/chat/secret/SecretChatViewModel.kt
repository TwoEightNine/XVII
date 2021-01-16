package com.twoeightnine.root.xvii.chats.messages.chat.secret

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.twoeightnine.root.xvii.background.longpoll.models.events.BaseMessageEvent
import com.twoeightnine.root.xvii.chats.messages.Interaction
import com.twoeightnine.root.xvii.chats.messages.chat.base.BaseChatMessagesViewModel
import com.twoeightnine.root.xvii.crypto.CryptoEngine
import com.twoeightnine.root.xvii.lg.L
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.model.attachments.Attachment
import com.twoeightnine.root.xvii.model.attachments.Doc
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.utils.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.security.MessageDigest

class SecretChatViewModel(
        api: ApiService,
        private val context: Context
) : BaseChatMessagesViewModel(api) {

    private var isExchangeStarted = false

    private val crypto by lazy {
        createCryptoEngine()
    }
    private val exchangeDisposable = getExchangeSubscription()

    private val keysSetLiveData = MutableLiveData<Boolean>()

    fun getKeysSet() = keysSetLiveData as LiveData<Boolean>

    fun isKeyRequired() = crypto.isKeyRequired()

    fun getFingerprint(): String = sha256(crypto.getFingerPrint())

    fun setKey(key: String) {
        crypto.setKey(key)
    }

    @Suppress("UnstableApiUsage")
    fun startExchange() {
        AsyncUtils.onIoThread(crypto::startExchange) { keyEx ->
            l("start exchange")
            ld(keyEx)
            sendData(keyEx)
            isExchangeStarted = true
            AsyncUtils.runDelayed(EXCHANGE_WAITING) {
                l("exchange not supported")
                isExchangeStarted = false
            }
        }
    }

    override fun loadMessages(offset: Int) {
        if (!isKeyRequired()) {
            super.loadMessages(offset)
        } else {
            interactionsLiveData.value = Wrapper(Interaction(Interaction.Type.CLEAR))
        }
    }

    override fun onMessageReceived(event: BaseMessageEvent) {
        if (!isKeyRequired()) {
            super.onMessageReceived(event)
        }
    }

    override fun attachPhoto(path: String, onAttached: (String, Attachment) -> Unit) {
        api.getDocUploadServer("doc")
                .subscribeSmart({ uploadServer ->
                    val plainFile = File(path)
                    val encryptFile = { crypto.encryptFile(plainFile) }
                    AsyncUtils.onIoThreadNullable(encryptFile) { encFile ->
                        encFile ?: return@onIoThreadNullable
                        val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), encFile)
                        val body = MultipartBody.Part.createFormData("file", encFile.name, requestFile)
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
                                    lw("uploading photo error", error)
                                    val message = error.message ?: "null"
                                    onErrorOccurred(message)
                                })
                    }

                }, { error ->
                    onErrorOccurred(error)
                    lw("getting uploading server error: $error")
                })
    }

    fun decryptDoc(doc: Doc, callback: (Boolean, String?) -> Unit) {
        downloadDoc(doc) { fileName ->
            val docFile = File(fileName)
            val decryptFile = { crypto.decryptFile(docFile) }
            AsyncUtils.onIoThreadNullable(decryptFile) { plainFile ->
                callback(plainFile != null, plainFile?.absolutePath)
            }
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
                        lw("error downloading $fileName: not written")
                    }
                }, {
                    lw("download file error", it)
                })
    }

    override fun prepareTextOut(text: String?) = when {
        text.isNullOrEmpty() -> ""
        else -> crypto.encrypt(text)
    }

    override fun prepareTextIn(text: String): String = crypto.decrypt(text) ?: ""

    private fun createCryptoEngine() = CryptoEngine.OnePeerUseCase(peerId)

    private fun ld(s: String) {
        L.tag(TAG).debug().log(s)
    }

    private fun l(s: String) {
        L.tag(TAG).log(s)
    }

    override fun lw(s: String, throwable: Throwable?) {
        L.tag(TAG)
                .throwable(throwable)
                .log(s)
    }

    override fun onCleared() {
        super.onCleared()
        exchangeDisposable.dispose()
    }

    private fun sha256(plain: ByteArray): String = MessageDigest
            .getInstance("SHA-256")
            .digest(plain)
            .map { Integer.toHexString(it.toInt() and 0xff) }
            .joinToString(separator = "") { if (it.length == 2) it else "0$it" }

    private fun getExchangeSubscription() =
            EventBus.subscribeExchangeEventReceived { event ->
                if (!event.isOut()) {
                    if (isExchangeStarted) {
                        l("receive support")
                        ld(event.text)
                        crypto.finishExchange(event.text)
                        isExchangeStarted = false
                        keysSetLiveData.value = true
                    } else {
                        l("receive exchange")
                        ld(event.text)
                        val ownKeys = crypto.supportExchange(event.text)
                        sendData(ownKeys)
                    }
                }
                deleteMessages(event.id.toString(), forAll = false)
            }


    companion object {
        private const val TAG = "secret chat"

        private const val EXCHANGE_WAITING = 10000L
    }
}