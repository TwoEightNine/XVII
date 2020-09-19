package com.twoeightnine.root.xvii.utils

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.*
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.DialogTitle
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.managers.Prefs


val SANS_SERIF_LIGHT = Typeface.create("sans-serif-light", Typeface.NORMAL)

object ColorManager {

    const val STROKE = 3 // in px

    const val PHOTO_STUB_URL = "https://dummyimage.com/200x200/%s/%s.png"

    const val MAIN_TAG = "main"
    const val LIGHT_TAG = "light"
    const val EXTRA_LIGHT_TAG = "extraLight"
    const val DARK_TAG = "dark"

    var shouldIgnore: Boolean = false
        private set

    var shapeColor: Int = 0
        private set

    var defaultColor: Int = 0
        private set

    var darkColor: Int = 0
        private set

    var mainColor: Int = 0
        private set

    var lightColor: Int = 0
        private set

    var extraLightColor: Int = 0
        private set

    var toolbarColor: Int = 0
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

fun Activity.stylize(color: Int = ColorManager.mainColor, isWhite: Boolean = false) {
    if (ColorManager.shouldIgnore) return
    if (isWhite && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        window.decorView.systemUiVisibility =
                window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }
}

private fun l(s: String) {
    Log.i("styler", s)
}

fun Toolbar.stylize() {
    if (ColorManager.shouldIgnore) return

    setBackgroundColor(ColorManager.mainColor)
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

fun TabLayout.stylize(tag: String = ColorManager.MAIN_TAG) {
    if (ColorManager.shouldIgnore) return

    setBackgroundColor(ColorManager.getColorByTag(tag))
}

fun ImageView.stylize(tag: String? = this.tag as? String, changeStroke: Boolean = true) {
    if (ColorManager.shouldIgnore) return
    tag?.let { drawable?.stylize(it, changeStroke) }
}

fun ImageView.stylizeAnyway(tag: String) {
    val color = ColorManager.getColorByTag(tag)
    with(drawable) {
        when (this) {
            is ShapeDrawable -> paint.color = color
            is GradientDrawable -> {
                setColor(color)
            }
            is ColorDrawable -> this.color = color
            is VectorDrawable -> setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        }
    }
}

fun Switch.stylize() {
    if (ColorManager.shouldIgnore) return

    thumbDrawable.setColorFilter(ColorManager.mainColor, PorterDuff.Mode.SRC_ATOP)
    trackDrawable.setColorFilter(ColorManager.lightColor, PorterDuff.Mode.SRC_ATOP)
}

fun SwitchCompat.stylize() {
    if (ColorManager.shouldIgnore) return

    thumbDrawable.setColorFilter(ColorManager.mainColor, PorterDuff.Mode.SRC_ATOP)
    trackDrawable.setColorFilter(ColorManager.lightColor, PorterDuff.Mode.SRC_ATOP)
}

fun FloatingActionButton.stylize() {
    if (ColorManager.shouldIgnore) return
    backgroundTintList = ColorStateList.valueOf(ColorManager.mainColor)
}

fun ProgressBar.stylize() {
    indeterminateDrawable.setColorFilter(ColorManager.mainColor, PorterDuff.Mode.MULTIPLY)
}

fun RadioButton.stylize() {
    val colorStateList = ColorStateList(
            arrayOf(
                    intArrayOf(-android.R.attr.state_enabled),
                    intArrayOf(android.R.attr.state_enabled)
            ),
            intArrayOf(
                    0xff888888.toInt(),
                    ColorManager.mainColor
            )
    )
    buttonTintList = colorStateList
}

fun Button.stylize() {
    if (ColorManager.shouldIgnore) return

    val b = background
    b.stylize(ColorManager.MAIN_TAG, changeStroke = false)
    background = b
}

fun TextView.stylize() {
    setTextColor(if (ColorManager.shouldIgnore) {
        ColorManager.lightColor
    } else {
        ColorManager.mainColor
    })
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
            findViewById<Button>(android.R.id.button1),
            findViewById<Button>(android.R.id.button2),
            findViewById<Button>(android.R.id.button3)
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
            window?.attributes = this
        }
    }
}

fun ViewGroup.stylizeAll(level: Int = 0) {
    if (ColorManager.shouldIgnore) return

//    stylize()
    stylizeColor()
    for (i in 0 until childCount) {
        val v = getChildAt(i)
        var r = ""
        for (j in 0 until level) {
            r += "--"
        }
        when (v) {
            is RadioButton -> v.stylize()
            is Switch -> v.stylize()
            is Button -> v.stylize()
            is FloatingActionButton -> v.stylize()
            is ImageView -> v.stylize()
            is Toolbar -> v.stylize()
            is TabLayout -> v.stylize()
            is ProgressBar -> v.stylize()
            is ViewGroup -> v.stylizeAll(level + 1)
        }
    }
}