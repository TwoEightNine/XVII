package com.twoeightnine.root.xvii.extensions

import global.msnthrp.xvii.uikit.extensions.upperIf

fun String.getInitials(uppercase: Boolean = true): String {
    return when {
        isBlank() -> ""
        else -> first().toString().upperIf(uppercase)
    }
}