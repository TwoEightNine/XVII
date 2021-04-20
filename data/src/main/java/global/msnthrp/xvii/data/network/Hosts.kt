package global.msnthrp.xvii.data.network

enum class Host(val baseUrl: String, val clientType: ClientType) {
    SAFE_PRIME("https://2ton.com.au/", ClientType.UNSAFE)
}

enum class ClientType {
    DEFAULT,
    UNSAFE
}