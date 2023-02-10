package com.twoeightnine.root.xvii.search

data class SearchDialog(

    var peerId: Int,

    var messageId: Int,

    var title: String,

    var text: String = "",

    var photo: String?,

    var isOnline: Boolean,

    var isOut: Boolean = true,

    var isChat: Boolean = false

)