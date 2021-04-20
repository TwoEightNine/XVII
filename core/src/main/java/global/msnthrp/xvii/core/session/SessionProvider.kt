package global.msnthrp.xvii.core.session

interface SessionProvider {

    var token: String?

    var userId: Int

    var fullName: String?

    var photo: String?

    var pin: String?

    val encryptionKey256: ByteArray

    fun clearAll()
}