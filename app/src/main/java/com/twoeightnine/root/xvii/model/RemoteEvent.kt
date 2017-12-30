package com.twoeightnine.root.xvii.model

data class RemoteEvent(var type: String,
                       var eventId: Int,
                       var target: Int,
                       var mainInfo: String,
                       var otherInfo: String) {

    companion object {
        val POPUP = "POPUP"
    }

}