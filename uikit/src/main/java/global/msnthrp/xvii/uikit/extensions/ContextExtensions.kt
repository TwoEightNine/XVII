package global.msnthrp.xvii.uikit.extensions

import android.app.Activity
import android.content.Context


fun Context.isValidForGlide() = this !is Activity || (!this.isDestroyed && !this.isFinishing)