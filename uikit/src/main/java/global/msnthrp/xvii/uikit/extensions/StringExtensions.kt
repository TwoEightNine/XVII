package global.msnthrp.xvii.uikit.extensions

import java.util.*

fun String.upperIf(lower: Boolean) = if (lower) upper() else this
fun String.lowerIf(lower: Boolean) = if (lower) lower() else this

fun String.upper() = toUpperCase(Locale.ROOT)
fun String.lower() = toLowerCase(Locale.ROOT)
