package com.twoeightnine.root.xvii.background.longpoll.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.background.longpoll.models.events.NewMessageEvent
import com.twoeightnine.root.xvii.crypto.CryptoEngine
import com.twoeightnine.root.xvii.lg.L
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.utils.notifications.NotificationUtils
import com.twoeightnine.root.xvii.utils.subscribeSmart
import javax.inject.Inject
import kotlin.random.Random

class KeyExchangeHandler {

    @Inject
    lateinit var api: ApiService

    private val crypto = CryptoEngine.common

    init {
        App.appComponent?.inject(this)
    }

    fun handleKeyExchange(context: Context, event: NewMessageEvent) {
        val peerId = event.peerId
        if (!event.isOut()) {
            if (crypto.isExchangeStarted(peerId)) {
                l("receive exchange support")
                ld(event.text)
                crypto.finishExchange(peerId, event.text)
            } else {
                l("receive exchange")
                ld(event.text)
                // show notification
                NotificationUtils.showKeyExchangeNotification(context, "$peerId")
                val ownKeys = crypto.supportExchange(peerId, event.text)
                sendData(peerId, ownKeys)
            }
        }
    }

    private fun sendData(peerId: Int, data: String) {
        api.sendMessage(peerId, Random.nextInt(), data)
                .subscribeSmart({}, { error ->
                    lw("send message: $error")
                })
    }

    private fun l(s: String) {
        L.tag(TAG).log(s)
    }

    private fun lw(s: String) {
        L.tag(TAG).warn().log(s)
    }

    private fun ld(s: String) {
        L.tag(TAG).debug().log(s)
    }

    companion object {
        private const val TAG = "key exchange"
    }

    class Receiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {

        }
    }
}