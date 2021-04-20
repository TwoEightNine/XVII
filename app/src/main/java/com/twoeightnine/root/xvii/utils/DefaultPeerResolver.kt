package com.twoeightnine.root.xvii.utils

import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.network.ApiService
import global.msnthrp.xvii.core.utils.PeerResolver
import global.msnthrp.xvii.data.db.AppDb
import global.msnthrp.xvii.data.dialogs.Dialog
import javax.inject.Inject

class DefaultPeerResolver : PeerResolver {

    @Inject
    lateinit var appDb: AppDb

    @Inject
    lateinit var api: ApiService

    init {
        App.appComponent?.inject(this)
    }

    override fun resolvePeers(peerIds: List<Int>): Map<Int, PeerResolver.ResolvedPeer> {
        val peers = appDb.dialogsDao()
                .getDialogsByPeerIds(peerIds)
                .blockingGet()
                .toResolvedPeers()
        val missingIds = peerIds.subtract(peers.map { it.peerId })

        return peers.map { it.peerId to it }.toMap()
    }

    private fun List<Dialog>.toResolvedPeers(): List<PeerResolver.ResolvedPeer> =
            map { dialog ->
                PeerResolver.ResolvedPeer(
                        peerId = dialog.peerId,
                        peerName = dialog.aliasOrTitle,
                        peerPhoto = dialog.photo ?: ""
                )
            }

}