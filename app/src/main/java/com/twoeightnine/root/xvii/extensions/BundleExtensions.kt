package com.twoeightnine.root.xvii.extensions

import android.os.Bundle

fun Bundle.getIntOrNull(key: String): Int? {
    return when {
        containsKey(key) -> getInt(key)
        else -> null
    }
}