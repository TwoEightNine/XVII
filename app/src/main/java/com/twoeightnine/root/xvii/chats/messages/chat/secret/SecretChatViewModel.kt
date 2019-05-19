package com.twoeightnine.root.xvii.chats.messages.chat.secret

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.twoeightnine.root.xvii.background.longpoll.models.events.NewMessageEvent
import com.twoeightnine.root.xvii.chats.messages.chat.base.BaseChatMessagesViewModel
import com.twoeightnine.root.xvii.crypto.CryptoEngine
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.utils.matchesXviiKeyEx

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
            sendMessage(it)
            isExchangeStarted = true
        }
    }

    override fun loadMessages(offset: Int) {
        if (!isKeyRequired()) {
            super.loadMessages(offset)
        }
    }

    override fun onMessageReceived(event: NewMessageEvent) {
        if (event.text.matchesXviiKeyEx()) {
            if (isExchangeStarted) {
                crypto.finishExchange(event.text)
                isExchangeStarted = false
                keysSetLiveData.value = true
            } else {
                val ownKeys = crypto.supportExchange(event.text)
                sendMessage(ownKeys)
            }
            deleteMessages(event.id.toString(), forAll = false)
        } else {
            super.onMessageReceived(event)
        }
    }

    override fun prepareTextOut(text: String?) = when(text) {
        null -> ""
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

    override fun preparePhoto(path: String, onPrepared: (String) -> Unit) {
        crypto.encryptFile(context, path, onPrepared)
    }
}