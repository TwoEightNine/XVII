package com.twoeightnine.root.xvii.background.longpoll.models.events

/**
 * used to handle deleted messages
 */
class InstallFlagsEvent(
        val id: Int,
        val flags: Int,
        val peerId: Int
) : BaseLongPollEvent() {

    override fun getType() = TYPE_INSTALL_FLAGS

    val isDeleted: Boolean
        get() = (flags and FLAG_DELETED) == FLAG_DELETED

    companion object {
        const val FLAG_DELETED = 128
    }
}