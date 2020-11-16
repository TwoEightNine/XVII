package com.twoeightnine.root.xvii.extensions

fun String.getInitials(): String {
    return when {
        isBlank() -> ""
        else -> first().toString()
    }
//    val words = split(' ')
//    return if (words.size < 2) {
//        first().toString()
//    } else {
//        "${words.first()[0]}${words.last()[0]}"
//    }
}