package global.msnthrp.xvii.core.utils


private const val CONVERSION_RADIX = 36

fun Int.toByteArray(): ByteArray = toString(CONVERSION_RADIX).toByteArray()

fun ByteArray.toInt(): Int = String(this).toIntOrNull(CONVERSION_RADIX) ?: 0