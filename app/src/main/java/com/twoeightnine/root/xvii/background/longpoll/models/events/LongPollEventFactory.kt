package com.twoeightnine.root.xvii.background.longpoll.models.events

import android.text.Html
import com.google.gson.internal.LinkedTreeMap
import com.twoeightnine.root.xvii.utils.asChatPeerId

object LongPollEventFactory {

    fun create(update: ArrayList<Any>): BaseLongPollEvent? {
        val type = (update[0] as Double).toInt()
        return when (type) {
            BaseLongPollEvent.TYPE_INSTALL_FLAGS -> InstallFlagsEvent(
                    update.asInt(1),
                    update.asInt(2),
                    update.asInt(3)
            )
            BaseLongPollEvent.TYPE_NEW_MESSAGE -> NewMessageEvent(
                    update.asInt(1),
                    update.asInt(2),
                    update.asInt(3),
                    update.asInt(4),
                    Html.fromHtml(update.asString(5)).toString(),
                    BaseMessageEvent.MessageInfo.fromLinkedTreeMap(update[6] as LinkedTreeMap<String, Any>)
            )
            BaseLongPollEvent.TYPE_EDIT_MESSAGE -> EditMessageEvent(
                    update.asInt(1),
                    update.asInt(2),
                    update.asInt(3),
                    update.asInt(4),
                    Html.fromHtml(update.asString(5)).toString(),
                    BaseMessageEvent.MessageInfo.fromLinkedTreeMap(update[6] as LinkedTreeMap<String, Any>)
            )
            BaseLongPollEvent.TYPE_READ_INCOMING -> ReadIncomingEvent(update.asInt(1), update.asInt(2))
            BaseLongPollEvent.TYPE_READ_OUTGOING -> ReadOutgoingEvent(update.asInt(1), update.asInt(2))
            BaseLongPollEvent.TYPE_ONLINE -> OnlineEvent(-update.asInt(1), update.asInt(2), update.asInt(3))
            BaseLongPollEvent.TYPE_OFFLINE -> OfflineEvent(-update.asInt(1), update.asInt(3))
            BaseLongPollEvent.TYPE_DELETE_MESSAGES -> DeleteMessagesEvent(update.asInt(1))
            BaseLongPollEvent.TYPE_TYPING -> TypingEvent(update.asInt(1))
            BaseLongPollEvent.TYPE_TYPING_CHAT -> TypingChatEvent(update.asInt(2).asChatPeerId())
            BaseLongPollEvent.TYPE_RECORDING_AUDIO -> RecordingAudioEvent(update.asInt(1))
            BaseLongPollEvent.TYPE_COUNT -> UnreadCountEvent(update.asInt(1))
            else -> null
        }
    }

    fun createAll(updates: ArrayList<ArrayList<Any>>): ArrayList<BaseLongPollEvent> {
        val events = arrayListOf<BaseLongPollEvent>()
        for (update in updates) {
            events.add(create(update) ?: continue)
        }
        return events
    }

    private fun ArrayList<Any>.asInt(position: Int) = (this[position] as Double).toInt()
    private fun ArrayList<Any>.asString(position: Int) = this[position] as String
}