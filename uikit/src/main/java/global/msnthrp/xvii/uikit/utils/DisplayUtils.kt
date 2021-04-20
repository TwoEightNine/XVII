package global.msnthrp.xvii.uikit.utils

import android.app.Activity
import android.util.DisplayMetrics
import android.view.WindowManager

object DisplayUtils {

    private lateinit var windowManager: WindowManager

    val screenWidth by lazy {
        DisplayMetrics().let {
            windowManager.defaultDisplay.getMetrics(it)
            it.widthPixels
        }
    }

    val screenHeight by lazy {
        DisplayMetrics().let {
            windowManager.defaultDisplay.getMetrics(it)
            it.heightPixels
        }
    }

    fun init(activity: Activity) {
        if (!::windowManager.isInitialized) {
            windowManager = activity.windowManager
        }
    }

}