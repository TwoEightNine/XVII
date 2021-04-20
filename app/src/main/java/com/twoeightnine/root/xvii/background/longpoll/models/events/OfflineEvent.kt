package com.twoeightnine.root.xvii.background.longpoll.models.events

data class OfflineEvent(val userId: Int,
                        val timeStamp: Int) : BaseLongPollEvent() {

    override fun getType() = TYPE_OFFLINE

}