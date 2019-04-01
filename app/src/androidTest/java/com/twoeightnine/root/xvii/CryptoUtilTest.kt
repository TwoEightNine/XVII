package com.twoeightnine.root.xvii

import androidx.test.runner.AndroidJUnit4
import com.twoeightnine.root.xvii.utils.crypto.CryptoUtil
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Created by twoeightnine on 1/25/18.
 */
@RunWith(AndroidJUnit4::class)
class CryptoUtilTest {

    @Test
    fun defaultKeys_isEqual() {
        val cryptoA = CryptoUtil(USER_A, USER_B)
        val cryptoB = CryptoUtil(USER_B, USER_A)
        assert(cryptoA.getFingerPrint() == cryptoB.getFingerPrint())
    }

    @Test
    fun resettingKeys_isEqual() {
        val crypto = CryptoUtil(USER_A, USER_B)
        val defaultFingerPrint = crypto.getFingerPrint()
        crypto.setUserKey(USER_KEY)
        crypto.resetKeys()
        assert(crypto.getFingerPrint() == defaultFingerPrint)
    }

    @Test
    fun customKeys_isEqual() {
        val cryptoA = CryptoUtil(USER_A, USER_B)
        val cryptoB = CryptoUtil(USER_B, USER_A)
        cryptoA.setUserKey(USER_KEY)
        cryptoB.setUserKey(USER_KEY)
        assert(cryptoA.getFingerPrint() == cryptoB.getFingerPrint())
    }

    @Test
    fun asymmetricExchange_isCorrect() {
        val cryptoA = CryptoUtil(USER_A, USER_B)
        val cryptoB = CryptoUtil(USER_B, USER_A)
        cryptoA.startKeyExchange {
            val partOfB = cryptoB.supportKeyExchange(it)
            cryptoA.finishKeyExchange(partOfB)
            assert(cryptoA.getFingerPrint() == cryptoB.getFingerPrint())
        }
    }

//    @Test
    fun encryption_isCorrect() {
        val crypto = CryptoUtil(USER_A, USER_B)
        assert(crypto.decrypt(crypto.encrypt(TEXT_SAMPLE)) == TEXT_SAMPLE)
    }

    companion object {
        const val USER_A = 12345678
        const val USER_B = 81726354

        const val USER_KEY = "userKeyUserKey"
        const val TEXT_SAMPLE = "all my life i wanted money and power respekt my mind or die from lead shower"
    }

}