package global.msnthrp.xvii.uikit.utils

import android.os.Build

object VersionUtils {

    fun supportsVibrationEffects() = atLeast(Build.VERSION_CODES.O)

    private fun atLeast(version: Int) = Build.VERSION.SDK_INT >= version
}