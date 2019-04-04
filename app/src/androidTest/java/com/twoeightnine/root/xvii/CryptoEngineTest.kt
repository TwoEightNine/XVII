package com.twoeightnine.root.xvii

import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.twoeightnine.root.xvii.crypto.CryptoEngine
import com.twoeightnine.root.xvii.crypto.bytesToHex
import junit.framework.Assert.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CryptoEngineTest {

    private lateinit var engine: CryptoEngine

    @Before
    fun before() {
        engine = CryptoEngine(InstrumentationRegistry.getTargetContext(), PEER_ID, testing = true)
    }

    @After
    fun after() {
        engine.resetStorage()
    }

    @Test
    fun wrap_correctness() {
        assertEquals(MESSAGE, CryptoEngine.unwrapData(CryptoEngine.wrapData(MESSAGE)))
        assertEquals(MESSAGE, CryptoEngine.unwrapKey(CryptoEngine.wrapKey(MESSAGE)))
    }

    @Test(expected = IllegalStateException::class)
    fun noKey_exception() {
        val enc = engine.encrypt(MESSAGE)
    }

    @Test
    fun encrypt_correctness() {
        engine.setKey(USER_KEY)
        val enc = engine.encrypt(MESSAGE)
        val dec = engine.decrypt(enc)
        assertEquals(true, dec.verified)
        assertNotNull(dec.bytes)
        assertEquals(bytesToHex(dec.bytes!!), bytesToHex(MESSAGE.toByteArray()))
    }

    @Test
    fun encrypt_reject() {
        engine.setKey(USER_KEY)
        var enc = engine.encrypt(MESSAGE)
        enc = enc.replaceFirst(enc[0], if (enc[0] == 'A') 'B' else 'A')
        val dec = engine.decrypt(enc)
        assertEquals(false, dec.verified)
        assertNull(dec.bytes)
    }

    companion object {

        const val MESSAGE = "ihfiuseiuctuayenxrysxiotybqpocwenxtpo23poweaf"
        const val USER_KEY = "someUserKey"

        const val PEER_ID = 1
    }
}