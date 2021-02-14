package global.msnthrp.xvii.core.analytics.model

data class CrashInfo(
        val message: String,
        val stackTrace: String,
        val logs: String
)