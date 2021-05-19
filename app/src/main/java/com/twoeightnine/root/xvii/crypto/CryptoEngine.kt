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

package com.twoeightnine.root.xvii.crypto

import android.content.Context
import com.twoeightnine.root.xvii.App
import global.msnthrp.xvii.core.crypto.engine.CryptoEngineUseCase
import global.msnthrp.xvii.core.crypto.safeprime.DefaultSafePrimeUseCase
import global.msnthrp.xvii.data.crypto.engine.Base64CryptoEngineEncoder
import global.msnthrp.xvii.data.crypto.engine.CacheCryptoEngineFileSource
import global.msnthrp.xvii.data.crypto.engine.PreferencesCryptoEngineRepo
import global.msnthrp.xvii.data.crypto.safeprime.DefaultSafePrimeRepo
import global.msnthrp.xvii.data.crypto.safeprime.storage.PreferencesSafePrimeDataSource
import global.msnthrp.xvii.data.crypto.safeprime.storage.retrofit.RetrofitSafePrimeDataSource
import java.io.File

object CryptoEngine {

    val common by lazy {
        createCryptoEngine(App.context)
    }

    private fun createCryptoEngine(context: Context): CryptoEngineUseCase {
        val safePrimeUseCase = DefaultSafePrimeUseCase(
                DefaultSafePrimeRepo(RetrofitSafePrimeDataSource(), PreferencesSafePrimeDataSource(context))
        )
        val cryptoEncoder = Base64CryptoEngineEncoder()
        val repo = PreferencesCryptoEngineRepo(context, cryptoEncoder)
        val fileSource = CacheCryptoEngineFileSource(context)
        return CryptoEngineUseCase(
                safePrimeUseCase, repo, cryptoEncoder, fileSource
        )
    }

    class OnePeerUseCase(private val peerId: Int) {

        fun isKeyRequired() = common.isKeyRequired(peerId)

        fun setKey(userKey: String, save: Boolean = true) {
            common.setKey(peerId, userKey, save)
        }

        fun resetStorage() {
            common.resetStorage()
        }

        fun startExchange(): String = common.startExchange(peerId)

        fun supportExchange(keyEx: String): String =
                common.supportExchange(peerId, keyEx)

        fun finishExchange(publicOtherWrapped: String) {
            common.setKey(peerId, publicOtherWrapped)
        }

        fun encrypt(message: String): String = common.encrypt(peerId, message)

        fun decrypt(message: String): String? = common.decrypt(peerId, message)

        fun encryptFile(file: File): File? = common.encryptFile(peerId, file)

        fun decryptFile(file: File): File? = common.decryptFile(peerId, file)

        fun getFingerPrint(): ByteArray = common.getFingerPrint(peerId)

    }
}