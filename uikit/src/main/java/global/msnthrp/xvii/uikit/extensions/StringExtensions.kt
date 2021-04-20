package global.msnthrp.xvii.uikit.extensions

import java.util.*

fun String.lowerIf(lower: Boolean) = if (lower) lower() else this

fun String.lower() = toLowerCase(Locale.ROOT)