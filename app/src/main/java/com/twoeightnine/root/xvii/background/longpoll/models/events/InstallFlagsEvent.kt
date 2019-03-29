package com.twoeightnine.root.xvii.background.longpoll.models.events

class InstallFlagsEvent(private val flags: Int) : BaseLongPollEvent() {

    override fun getType() = TYPE_INSTALL_FLAGS

    val isDeleted: Boolean
        get() = (flags and FLAG_DELETED) == FLAG_DELETED

    companion object {
        const val FLAG_DELETED = 128
    }
}