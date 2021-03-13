package com.twoeightnine.root.xvii.utils.deeplink.cases

import android.content.Intent
import com.twoeightnine.root.xvii.chatowner.fragments.BaseChatOwnerFragment
import com.twoeightnine.root.xvii.extensions.getIntOrNull
import com.twoeightnine.root.xvii.utils.deeplink.DeepLinkHandler

object ChatOwnerCase : DeepLinkHandler.Case<Int> {

    override fun parseIntent(intent: Intent): Int? {
        return when (intent.action) {
            Intent.ACTION_VIEW -> intent.data?.lastPathSegment?.replace("id", "")?.toIntOrNull()
            else -> intent.extras?.getIntOrNull(BaseChatOwnerFragment.ARG_PEER_ID)
        }
    }

    override fun getResult(intent: Intent): DeepLinkHandler.Result =
            when (val peerId = parseIntent(intent)) {
                null -> DeepLinkHandler.Result.Unknown
                else -> DeepLinkHandler.Result.ChatOwner(peerId)
            }
}