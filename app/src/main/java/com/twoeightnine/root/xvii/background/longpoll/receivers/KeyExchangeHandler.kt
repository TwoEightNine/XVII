package com.twoeightnine.root.xvii.background.longpoll.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.background.longpoll.models.events.NewMessageEvent
import com.twoeightnine.root.xvii.crypto.CryptoEngine
import com.twoeightnine.root.xvii.lg.L
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.utils.AsyncUtils
import com.twoeightnine.root.xvii.utils.DefaultPeerResolver
import com.twoeightnine.root.xvii.utils.notifications.NotificationUtils
import com.twoeightnine.root.xvii.utils.subscribeSmart
import global.msnthrp.xvii.core.utils.PeerResolver
import javax.inject.Inject
import kotlin.random.Random

class KeyExchangeHandler {

    @Inject
    lateinit var api: ApiService

    private val crypto = CryptoEngine.common

    private val peerResolver by lazy {
        DefaultPeerResolver()
    }

    init {
        App.appComponent?.inject(this)
    }

    fun handleKeyExchange(context: Context, event: NewMessageEvent) {
        val peerId = event.peerId
        val exchange = event.text
        if (!event.isOut()) {
            if (!crypto.isNewExchange(exchange)) {
                l("receive exchange support: ${exchange.takeLast(8)}")
                crypto.finishExchange(peerId, exchange)
            } else {
                l("receive exchange: ${exchange.takeLast(8)}")

                val callback = { peer: PeerResolver.ResolvedPeer? ->
                    ld("peer resolved: $peer")
                    NotificationUtils.showKeyExchangeNotification(
                            context, peer?.peerName ?: "id$peerId", peerId, exchange
                    )
                }

                AsyncUtils.onIoThreadNullable({ resolvePeer(peerId) }, { callback(null) }, callback)
            }
        }
        deleteMessages(event.id.toString())
    }

    private fun resolvePeer(peerId: Int): PeerResolver.ResolvedPeer? =
            peerResolver.resolvePeers(listOf(peerId))[peerId]

    private fun deleteMessages(messageIds: String) {
        api.deleteMessages(messageIds, 0)
                .subscribeSmart({}, {})
    }

    private fun l(s: String) {
        L.tag(TAG).log(s)
    }

    private fun ld(s: String) {
        L.tag(TAG).debug().log(s)
    }

    companion object {
        const val ACTION_DENY_EXCHANGE = "actionDenyExchange"
        const val ACTION_ACCEPT_EXCHANGE = "actionAcceptExchange"

        const val ARG_PEER_ID = "peerId"
        const val ARG_EXCHANGE_TEXT = "exchangeText"

        private const val TAG = "key exchange"
    }

    class Receiver : BroadcastReceiver() {

        @Inject
        lateinit var api: ApiService

        override fun onReceive(context: Context?, intent: Intent?) {
            App.appComponent?.inject(this)
            intent ?: return

            if (intent.action != ACTION_ACCEPT_EXCHANGE) return

            val exchangeText = intent.extras?.getString(ARG_EXCHANGE_TEXT) ?: return
            val peerId = intent.extras?.getInt(ARG_PEER_ID) ?: return

            val ownKeys = CryptoEngine.common.supportExchange(peerId, exchangeText)
            sendData(peerId, ownKeys)
            context?.also(NotificationUtils::hideAllExchangeNotifications)
        }

        private fun sendData(peerId: Int, data: String) {
            api.sendMessage(peerId, Random.nextInt(), data)
                    .subscribeSmart({}, { error ->
                        L.tag(TAG).warn().log("send message: $error")
                    })
        }
    }
}