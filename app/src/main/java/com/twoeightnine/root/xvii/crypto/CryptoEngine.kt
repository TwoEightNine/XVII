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