package global.msnthrp.xvii.uikit.utils

import global.msnthrp.xvii.uikit.extensions.lower

object ExtensionUtils {

    private val imageExtensions = listOf("jpg", "jpeg", "png")

    fun isImage(path: String): Boolean {
        val lowerPath = path.lower()
        for (ext in imageExtensions) {
            if (lowerPath.endsWith(".$ext")) {
                return true
            }
        }
        return false
    }

}