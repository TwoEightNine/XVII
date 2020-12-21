package global.msnthrp.xvii.uikit.utils.color

object DocColors {

    fun getColorByExtension(ext: String): Int? = when(ext) {
        "pdf" -> 0xa81e16
        "doc", "docx", "odf", "rtf" -> 0x6e79ed
        "xls", "xlsx", "csv" -> 0x3e9d5a
        "zip", "rar", "7z" -> 0x92d159
        "ppt", "pptx" -> 0xca4325
        "torrent" -> 0x2c8b3e
        "otf", "ttf" -> 0x313131
        else -> null
    }?.let { it or 0xff000000.toInt() }
}