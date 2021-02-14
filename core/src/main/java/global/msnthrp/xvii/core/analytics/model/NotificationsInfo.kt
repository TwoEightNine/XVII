package global.msnthrp.xvii.core.analytics.model

data class NotificationsInfo(
        val showPrivateNotifications: Boolean,
        val showPrivateContent: Boolean,
        val showPrivateName: Boolean,

        val showOtherNotifications: Boolean,
        val showOtherContent: Boolean,

        val useStylish: Boolean
)