package com.twoeightnine.root.xvii.utils

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.*
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.DialogTitle
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.managers.Prefs


val SANS_SERIF_LIGHT: Typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)

object ColorManager {

    const val STROKE = 3 // in px

    private const val PHOTO_STUB_URL = "https://dummyimage.com/200x200/%s/%s.png"

    const val MAIN_TAG = "main"
    private const val LIGHT_TAG = "light"
    private const val EXTRA_LIGHT_TAG = "extraLight"
    const val DARK_TAG = "dark"

    var shouldIgnore: Boolean = false
        private set

    var shapeColor: Int = 0
        private set

    private var defaultColor: Int = 0

    private var darkColor: Int = 0

    var mainColor: Int = 0
        private set

    var lightColor: Int = 0
        private set

    var extraLightColor: Int = 0
        private set

    private var toolbarColor: Int = 0
        get() = if (Prefs.isLightTheme) mainColor else field

    fun init(context: Context) {
        mainColor = Prefs.color
        val other = getFromMain(mainColor)
        darkColor = other[0]
        lightColor = other[2]
        extraLightColor = other[3]
        defaultColor = ContextCompat.getColor(context, R.color.avatar)
        shapeColor = ContextCompat.getColor(context, R.color.shape)
        toolbarColor = ContextCompat.getColor(context, R.color.toolbar)
        shouldIgnore = !Prefs.isLightTheme
    }

    fun getColorByTag(tag: String) = when (tag) {
        LIGHT_TAG -> lightColor
        EXTRA_LIGHT_TAG -> extraLightColor
        DARK_TAG -> darkColor
        else -> mainColor
    }

    fun getFromMain(mainColor: Int = this.mainColor): IntArray { //[dark, main, light, extraLight]

        val dark = -120
        val light = 75 // #b0b0b0
        val extraLight = 25 // #e0e0e0

        val result = IntArray(4)

        result[0] = getOtherColor(mainColor, dark)
        result[1] = mainColor
        result[2] = getOtherColor(mainColor, light)
        result[3] = getOtherColor(mainColor, extraLight)

        return result
    }

    fun getPhotoStub(): String {
        val color = if (Prefs.isLightTheme) lightColor else darkColor
        val colorHex = String.format("%X", color).substring(2)
        return String.format(PHOTO_STUB_URL, colorHex, colorHex)
    }

    private fun getOtherColor(color: Int, coeff: Int): Int {
        val red = color and 0x00ffffff shr 16
        val green = color and 0x0000ffff shr 8
        val blue = color and 0x000000ff
        return Color.argb(255, shiftColor(red, coeff), shiftColor(green, coeff), shiftColor(blue, coeff))
    }

    private fun shiftColor(color: Int, shift: Int) =
            if (shift > 0) {
                255 - shift + color * shift / 255
            } else {
                color * -shift / 255
            }
}

fun ViewGroup.stylizeAsMessage(level: Int, hide: Boolean = false) {
    when {
        hide -> {
            (background as GradientDrawable)
                    .setColor(Color.TRANSPARENT)
        }
        ColorManager.shouldIgnore -> {
            (background as GradientDrawable)
                    .setColor(ColorManager.shapeColor)
        }
        level % 2 == 0 -> {
            (background as GradientDrawable)
                    .setColor(ColorManager.lightColor)
        }
        else -> {
            (background as GradientDrawable)
                    .setColor(ColorManager.extraLightColor)
        }
    }
}

@SuppressWarnings
fun Drawable.stylize(tag: String, changeStroke: Boolean = true) {
    if (ColorManager.shouldIgnore) return

    val color = ColorManager.getColorByTag(tag)
    when (this) {
        is ShapeDrawable -> paint.color = color
        is GradientDrawable -> {
            setColor(color)
            if (changeStroke) {
                setStroke(ColorManager.STROKE, Color.WHITE)
            }
        }
        is ColorDrawable -> this.color = color
        is VectorDrawable -> setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
    }
}

fun CardView.stylize(tag: String = ColorManager.DARK_TAG) {
    if (ColorManager.shouldIgnore) return

    setCardBackgroundColor(ColorManager.getColorByTag(tag))
}

fun BottomNavigationView.stylize() {

    val states = arrayOf(
            intArrayOf(android.R.attr.state_selected, android.R.attr.state_checked),
            intArrayOf()
    )

    val gray = if (ColorManager.shouldIgnore) Color.DKGRAY else Color.LTGRAY
    val colors = intArrayOf(ColorManager.mainColor, gray)
    itemIconTintList = ColorStateList(states, colors)
}

fun ViewGroup.stylizeColor() {
    if (ColorManager.shouldIgnore) return
    (tag as? String)?.let { setBackgroundColor(ColorManager.getColorByTag(it)) }
}

@SuppressWarnings
fun ViewGroup.stylize(changeStroke: Boolean = true) {
    if (ColorManager.shouldIgnore) return
    (tag as? String)?.let { background.stylize(it, changeStroke) }
}

fun ImageView.stylize(tag: String? = this.tag as? String, changeStroke: Boolean = true) {
    if (ColorManager.shouldIgnore) return
    tag?.let { drawable?.stylize(it, changeStroke) }
}

fun Switch.stylize() {
    if (ColorManager.shouldIgnore) return

    thumbDrawable.setColorFilter(ColorManager.mainColor, PorterDuff.Mode.SRC_ATOP)
    trackDrawable.setColorFilter(ColorManager.lightColor, PorterDuff.Mode.SRC_ATOP)
}

fun AlertDialog.stylize(keepFont: Boolean = false, warnPositive: Boolean = false) {

    val typeface = Typeface.createFromAsset(context.resources.assets, "fonts/medium.ttf")

    val mainText = ContextCompat.getColor(context, R.color.main_text)
    val otherText = ContextCompat.getColor(context, R.color.other_text)
    val popupColor = ContextCompat.getColor(context, R.color.popup)

    window?.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.shape_context_dialog))

    findViewById<View>(R.id.contentPanel)?.setBackgroundColor(popupColor)
    findViewById<View>(R.id.buttonPanel)?.setBackgroundColor(popupColor)
    findViewById<View>(R.id.topPanel)?.setBackgroundColor(popupColor)

    findViewById<TextView>(android.R.id.message)?.apply {
        if (!keepFont) {
//            typeface = SANS_SERIF_LIGHT
            textSize = 18f
        }
        setTextColor(mainText)
    }
    findViewById<DialogTitle>(R.id.alertTitle)?.apply {
        //        typeface = SANS_SERIF_LIGHT
        textSize = 20f
        setTextColor(mainText)
    }
    for (btn in arrayListOf(
            findViewById(android.R.id.button1),
            findViewById<Button>(android.R.id.button2),
            findViewById(android.R.id.button3)
    )) {
        btn?.apply {
            this.typeface = typeface
            textSize = 18f
            isAllCaps = false
            setTextColor(otherText)
        }

    }

    if (warnPositive) {
        findViewById<Button>(android.R.id.button1)
                ?.setTextColor(ContextCompat.getColor(context, R.color.warn_text))
    }

    if (!keepFont) {
        WindowManager.LayoutParams().apply {
            copyFrom(window?.attributes)
            width = pxFromDp(context, 280)
            y = pxFromDp(context, 40)
            gravity = Gravity.BOTTOM
            window?.attributes = this
        }
    }
}

fun ViewGroup.stylizeAll(level: Int = 0) {
    if (ColorManager.shouldIgnore) return

    stylizeColor()
    for (i in 0 until childCount) {
        val v = getChildAt(i)
        var r = ""
        for (j in 0 until level) {
            r += "--"
        }
        when (v) {
            is Switch -> v.stylize()
            is ImageView -> v.stylize()
            is ViewGroup -> v.stylizeAll(level + 1)
        }
    }
}