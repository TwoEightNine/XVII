package com.twoeightnine.root.xvii.journal.online.model

data class OnlineInfo(
        val userId: Int,
        val userName: String,
        val events: List<OnlineEvent>
)