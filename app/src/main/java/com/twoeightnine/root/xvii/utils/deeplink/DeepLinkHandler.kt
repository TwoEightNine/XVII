package com.twoeightnine.root.xvii.utils.deeplink

import android.content.Intent
import com.twoeightnine.root.xvii.utils.deeplink.cases.ChatOwnerCase

class DeepLinkHandler {

    private val cases = listOf(
        ChatOwnerCase
    )

    fun handle(intent: Intent): Result {
        for (case in cases) {
            val result = case.getResult(intent)
            if (result != Result.Unknown) {
                return result
            }
        }
        return Result.Unknown
    }

    sealed class Result {

        data class ChatOwner(val peerId: Int) : Result()

        object Unknown : Result()
    }

    interface Case<T : Any> {
        fun parseIntent(intent: Intent): T?

        fun getResult(intent: Intent): Result
    }

}