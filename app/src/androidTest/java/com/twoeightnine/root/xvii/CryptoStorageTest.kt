package com.twoeightnine.root.xvii

import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.twoeightnine.root.xvii.crypto.CryptoStorage
import com.twoeightnine.root.xvii.crypto.bytesToHex
import junit.framework.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CryptoStorageTest {

    private lateinit var storage: CryptoStorage

    @Before
    fun before() {
        storage = CryptoStorage(InstrumentationRegistry.getTargetContext(), "cryptoTest")
    }

    @After
    fun after() {
        storage.clear()
    }

    @Test
    fun storage_peerKey() {
        assertEquals(storage.hasKey(PEER_ID), false)
        storage.saveKey(PEER_ID, PEER_KEY)
        assertEquals(storage.hasKey(PEER_ID), true)
        assertEquals(bytesToHex(PEER_KEY), bytesToHex(storage.getKey(PEER_ID)))
        storage.removeKey(PEER_ID)
        assertEquals(storage.hasKey(PEER_ID), false)
    }

    companion object {

        const val PEER_ID = 1
        val PEER_KEY = "someKey".toByteArray()
    }
}