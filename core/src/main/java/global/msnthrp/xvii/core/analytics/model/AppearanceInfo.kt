package global.msnthrp.xvii.core.analytics.model

data class AppearanceInfo(
        val color: Int,
        val isLightTheme: Boolean,

        val isAppleEmoji: Boolean,
        val isCustomBackground: Boolean,
        val messageTextSize: Int,

        val showSeconds: Boolean,
        val useLowerCase: Boolean

)