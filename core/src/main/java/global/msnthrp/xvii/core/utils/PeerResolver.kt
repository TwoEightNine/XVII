package global.msnthrp.xvii.core.utils

interface PeerResolver {

    fun resolvePeers(peerIds: List<Int>): Map<Int, ResolvedPeer>

    data class ResolvedPeer(
            val peerId: Int,
            val peerName: String,
            val peerPhoto: String
    )
}