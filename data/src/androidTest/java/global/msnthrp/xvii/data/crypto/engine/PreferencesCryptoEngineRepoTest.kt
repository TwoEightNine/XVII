package global.msnthrp.xvii.data.crypto.engine

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import global.msnthrp.xvii.core.crypto.CryptoUtils
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PreferencesCryptoEngineRepoTest {

    private val bytes1 = "some secret key".toByteArray()
    private val bytes2 = "other secret key".toByteArray()

    @Test
    fun getNonExistingKey_returnNull() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repo = PreferencesCryptoEngineRepo(context, Base64CryptoEngineEncoder())
        val key = repo.getKeyOrNull(3301)

        Assert.assertNull(key)
    }

    @Test
    fun getExistingKey_returnKey() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repo = PreferencesCryptoEngineRepo(context, Base64CryptoEngineEncoder())

        val peerId1 = 3301
        val peerId2 = 3302

        repo.setKey(peerId1, bytes1)
        val key1 = repo.getKeyOrNull(peerId1) ?: "failed".toByteArray()
        Assert.assertEquals(CryptoUtils.bytesToHex(key1), CryptoUtils.bytesToHex(bytes1))

        repo.setKey(peerId2, bytes2)
        val key2 = repo.getKeyOrNull(peerId2) ?: "failed".toByteArray()
        Assert.assertEquals(CryptoUtils.bytesToHex(key2), CryptoUtils.bytesToHex(bytes2))

        Assert.assertNotEquals(CryptoUtils.bytesToHex(key1), CryptoUtils.bytesToHex(key2))
    }

    @Test
    fun overrideKey_returnRecent() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repo = PreferencesCryptoEngineRepo(context, Base64CryptoEngineEncoder())

        val peerId = 3301

        repo.setKey(peerId, bytes1)
        val key1 = repo.getKeyOrNull(peerId) ?: "failed".toByteArray()
        Assert.assertEquals(CryptoUtils.bytesToHex(key1), CryptoUtils.bytesToHex(bytes1))

        repo.setKey(peerId, bytes2)
        val key2 = repo.getKeyOrNull(peerId) ?: "failed".toByteArray()
        Assert.assertEquals(CryptoUtils.bytesToHex(key2), CryptoUtils.bytesToHex(bytes2))

        Assert.assertNotEquals(CryptoUtils.bytesToHex(key1), CryptoUtils.bytesToHex(key2))
    }

    @Test
    fun clear_returnNull() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repo = PreferencesCryptoEngineRepo(context, Base64CryptoEngineEncoder())

        val peerId = 3301

        repo.setKey(peerId, bytes1)
        val key1 = repo.getKeyOrNull(peerId) ?: "failed".toByteArray()
        Assert.assertEquals(CryptoUtils.bytesToHex(key1), CryptoUtils.bytesToHex(bytes1))

        repo.clearAll()
        val key2 = repo.getKeyOrNull(peerId)
        Assert.assertNull(key2)
    }

}