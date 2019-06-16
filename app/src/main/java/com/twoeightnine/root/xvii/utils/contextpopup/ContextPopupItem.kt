package com.twoeightnine.root.xvii.utils.contextpopup

data class ContextPopupItem(
        val iconRes: Int,
        val textRes: Int,
        val onClick: () -> Unit
)