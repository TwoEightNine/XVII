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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

class SecretChatViewModel(
        api: ApiService,
        private val context: Context
) : BaseChatMessagesViewModel(api) {

    private val crypto by lazy {
        createCryptoEngine()
    }

    private val keysSetLiveData = MutableLiveData<Boolean>()

    fun getKeysSet() = keysSetLiveData as LiveData<Boolean>

    fun isKeyRequired() = crypto.isKeyRequired()

    fun getFingerprint(): String = sha256(crypto.getFingerPrint())

    fun setKey(key: String) {
        crypto.setKey(key)
    }

    fun startExchange() {
        AsyncUtils.onIoThread(crypto::startExchange) { keyEx ->
            l("start exchange: ${keyEx.takeLast(8)}")
            sendData(keyEx)
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
        attachFile(path, "photo", onAttached)
    }

    override fun attachDoc(path: String, onAttached: (String, Attachment) -> Unit) {
        attachFile(path, "doc", onAttached)
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

    private fun attachFile(path: String, what: String = "???", onAttached: (String, Attachment) -> Unit) {
        api.getDocUploadServer("doc")
                .subscribeSmart({ uploadServer ->
                    val plainFile = File(path)
                    val encryptFile = { crypto.encryptFile(plainFile) }
                    AsyncUtils.onIoThreadNullable(encryptFile) { encFile ->
                        encFile ?: return@onIoThreadNullable
                        val requestFile = encFile.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                        val body = MultipartBody.Part.createFormData("file", encFile.name, requestFile)
                        api.uploadDoc(uploadServer.uploadUrl ?: "", body)
                                .compose(applySchedulers())
                                .subscribe({ uploaded ->
                                    api.saveDoc(uploaded.file ?: return@subscribe)
                                            .subscribeSmart({ attachment ->
                                                onAttached(path, attachment)
                                            }, { error ->
                                                onErrorOccurred(error)
                                                lw("save uploaded $what error: $error")
                                            })
                                }, { error ->
                                    lw("uploading $what error", error)
                                    val message = error.message ?: "null"
                                    onErrorOccurred(message)
                                })
                    }

                }, { error ->
                    onErrorOccurred(error)
                    lw("getting uploading server error: $error")
                })
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

    override fun ld(s: String) {
        L.tag(TAG).debug().log(s)
    }

    override fun l(s: String) {
        L.tag(TAG).log(s)
    }

    override fun lw(s: String, throwable: Throwable?) {
        L.tag(TAG)
                .throwable(throwable)
                .log(s)
    }

    private fun sha256(plain: ByteArray): String = MessageDigest
            .getInstance("SHA-256")
            .digest(plain)
            .map { Integer.toHexString(it.toInt() and 0xff) }
            .joinToString(separator = "") { if (it.length == 2) it else "0$it" }


    companion object {

        private val EXCHANGE_FAILURE_THRESHOLD = TimeUnit.SECONDS.toMillis(10L)
        private const val TAG = "secret chat"
    }
}