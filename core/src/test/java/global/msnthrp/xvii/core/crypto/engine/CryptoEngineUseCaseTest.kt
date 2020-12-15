package global.msnthrp.xvii.core.crypto.engine

import global.msnthrp.xvii.core.crypto.CryptoUtils
import global.msnthrp.xvii.core.crypto.safeprime.SafePrimeUseCase
import global.msnthrp.xvii.core.crypto.safeprime.entity.SafePrime
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import java.io.File
import java.math.BigInteger

class CryptoEngineUseCaseTest {

    @Rule
    @JvmField
    val thrown: ExpectedException = ExpectedException.none()

    @Test
    fun emptyStorage_keyRequired() {
        val useCase = createUseCase()

        Assert.assertEquals(useCase.isKeyRequired(), true)
    }

    @Test
    fun encryptWithoutKey_throw() {
        thrown.expect(IllegalStateException::class.java)
        val useCase = createUseCase()
        useCase.encrypt("test message")
    }

    @Test
    fun encryption_valid() {
        val userKey = "test key"
        val useCase = createUseCase()
        useCase.setKey(userKey)

        val message = "my very very ervy very very ervyervyerv secret meeeeeeeeeeeeeeeeeeeeeesage"
        val enc = useCase.encrypt(message)
        val dec = useCase.decrypt(enc)

        Assert.assertEquals(message, dec)

        val useCase2 = createUseCase()
        useCase2.setKey(userKey)
        val dec2 = useCase2.decrypt(enc)

        Assert.assertEquals(message, dec2)
    }

    @Test
    fun filledStorage_everyPeerHasOwnKey() {
        val repo = TestCryptoEngineRepo()
        val peerId1 = PEER_ID
        val peerId2 = PEER_ID + 1

        repo.setKey(peerId1, CryptoUtils.sha256(peerId1.toString().toByteArray()))
        repo.setKey(peerId2, CryptoUtils.sha256(peerId2.toString().toByteArray()))

        val useCase1 = createUseCase(peerId = peerId1, repo = repo)
        val fingerprint1 = useCase1.getFingerPrint()

        val useCase2 = createUseCase(peerId = peerId2, repo = repo)
        val fingerprint2 = useCase2.getFingerPrint()

        Assert.assertNotEquals(CryptoUtils.bytesToHex(fingerprint1), CryptoUtils.bytesToHex(fingerprint2))
    }

    @Test
    fun filledStorage_peersKeyDoesntChange() {
        val repo = TestCryptoEngineRepo()

        repo.setKey(PEER_ID, CryptoUtils.sha256(PEER_ID.toString().toByteArray()))

        val useCase1 = createUseCase(repo = repo)
        val fingerprint1 = useCase1.getFingerPrint()

        val useCase2 = createUseCase(repo = repo)
        val fingerprint2 = useCase2.getFingerPrint()

        Assert.assertEquals(CryptoUtils.bytesToHex(fingerprint1), CryptoUtils.bytesToHex(fingerprint2))
    }

    @Test
    fun diffieHellman_sharedKeyIsTheSame() {
        val repo1 = TestCryptoEngineRepo()
        val repo2 = TestCryptoEngineRepo()

        val peerId1 = PEER_ID
        val peerId2 = PEER_ID + 1

        val useCase1 = createUseCase(peerId = peerId1, repo = repo1)
        val useCase2 = createUseCase(peerId = peerId2, repo = repo2)

        val keyEx1 = useCase1.startExchange()
        val keyEx2 = useCase2.supportExchange(keyEx1)
        useCase1.finishExchange(keyEx2)

        val sharedKey1 = useCase1.getFingerPrint()
        val sharedKey2 = useCase2.getFingerPrint()

        Assert.assertEquals(CryptoUtils.bytesToHex(sharedKey1), CryptoUtils.bytesToHex(sharedKey2))

        val storedKey1 = repo1.getKeyOrNull(peerId1) ?: "not".toByteArray()
        val storedKey2 = repo2.getKeyOrNull(peerId2) ?: "equal".toByteArray()

        Assert.assertEquals(CryptoUtils.bytesToHex(storedKey1), CryptoUtils.bytesToHex(storedKey2))
    }

    private fun createUseCase(peerId: Int = PEER_ID, repo: CryptoEngineRepo = TestCryptoEngineRepo()) = CryptoEngineUseCase(
            peerId = peerId,
            safePrimeUseCase = TestSafePrimeUseCase(),
            cryptoEngineRepo = repo,
            cryptoEngineEncoder = TestCryptoEngineEncoder(),
            cryptoEngineFileSource = TestCryptoEngineFileSource()
    )

    companion object {

        private const val PEER_ID = 1753

        private const val PRIME = "429960845873088536599738146849398890197656281978746052260302" +
                "647466290912363305996665498753182126120318295110244403964643426486915779918338905631403" +
                "871028184935255084712703423613529366297760543627384218281702559557613931230981025214701" +
                "436292843374882547050410898749274017561380961864641986822934974094546625703373934105581" +
                "804151000069136171694933799628596746360744089987859632744266134003621159571422862980144" +
                "043377717793249604329463406248840895370685965329721512935512704815510998868368597285047" +
                "022910731679048010756286396108128504010975404344672924191827643103234869173733652744217" +
                "51734483875743936086632531541480503"

        private val DEFAULT_SAFE_PRIME = SafePrime(
                p = PRIME,
                q = ((BigInteger(PRIME) - BigInteger.ONE) / BigInteger("2")).toString(10),
                g = "3"
        )
    }

    private class TestSafePrimeUseCase : SafePrimeUseCase {
        override fun loadSafePrime(): SafePrime = DEFAULT_SAFE_PRIME
    }

    private class TestCryptoEngineRepo : CryptoEngineRepo {

        private var keys = hashMapOf<Int, ByteArray>()

        override fun getKeyOrNull(peerId: Int): ByteArray? = keys.getOrDefault(peerId, null)

        override fun setKey(peerId: Int, key: ByteArray) {
            keys[peerId] = key
        }

        override fun clearAll() {
            keys.clear()
        }
    }

    private class TestCryptoEngineEncoder : CryptoEngineEncoder {
        override fun encode(bytes: ByteArray): String = CryptoUtils.bytesToHex(bytes)

        override fun decode(string: String): ByteArray {
            val byteList = arrayListOf<Byte>()
            var pos = 0
            while (pos < string.length) {
                val byte = string.substring(pos, pos + 2).toInt(16).toByte()
                byteList.add(byte)
                pos += 2
            }
            return byteList.toByteArray()
        }
    }

    private class TestCryptoEngineFileSource : CryptoEngineFileSource {
        override fun readFromFile(file: File): ByteArray? =
                file.name.toByteArray()

        override fun writeToFile(fileName: String, bytes: ByteArray): File? =
                File(String(bytes))
    }
}