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