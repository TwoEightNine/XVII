package global.msnthrp.xvii.core.journal.model

sealed class JournalEvent(
        val peerId: Int,

        val timeStamp: Long
) {

    sealed class MessageJE(
            peerId: Int,
            timeStamp: Long,

            val messageId: Int
    ) : JournalEvent(peerId, timeStamp) {

        class DeletedMessageJE(
                peerId: Int,
                timeStamp: Long,
                messageId: Int
        ) : MessageJE(peerId, timeStamp, messageId)

        class NewMessageJE(
                peerId: Int,
                timeStamp: Long,
                messageId: Int,

                val fromId: Int = peerId,
                val messageText: String
        ) : MessageJE(peerId, timeStamp, messageId)

        class EditedMessageJE(
                peerId: Int,
                timeStamp: Long,
                messageId: Int,

                val fromId: Int = peerId,
                val messageText: String
        ) : MessageJE(peerId, timeStamp, messageId)

        class ReadMessageJE(
                peerId: Int,
                timeStamp: Long,
                messageId: Int
        ) : MessageJE(peerId, timeStamp, messageId)

    }

    sealed class ActivityJE(
            peerId: Int,
            timeStamp: Long,

            val fromId: Int = peerId
    ) : JournalEvent(peerId, timeStamp) {

        class TypingActivityJE(peerId: Int, timeStamp: Long, fromId: Int = peerId) : ActivityJE(peerId, timeStamp, fromId)

        class RecordingActivityJE(peerId: Int, timeStamp: Long, fromId: Int = peerId) : ActivityJE(peerId, timeStamp, fromId)
    }

    sealed class StatusJE(
            peerId: Int,
            timeStamp: Long
    ) : JournalEvent(peerId, timeStamp) {

        class OnlineStatusJE(
                peerId: Int,
                timeStamp: Long,

                val deviceCode: Int
        ) : StatusJE(peerId, timeStamp)

        class OfflineStatusJE(peerId: Int, timeStamp: Long) : StatusJE(peerId, timeStamp)
    }
}
