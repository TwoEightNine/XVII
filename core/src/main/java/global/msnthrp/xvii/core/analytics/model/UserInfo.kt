package global.msnthrp.xvii.core.analytics.model

data class UserInfo(
        val userId: String,

        val deviceBrand: String,
        val deviceModel: String,

        val apiVersion: Int,
        val appVersion: String
)