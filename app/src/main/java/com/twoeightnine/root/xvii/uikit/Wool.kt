package com.twoeightnine.root.xvii.uikit

import android.content.Context
import android.graphics.Typeface

object Wool {

    private const val DIR = "fonts/"
    private const val EXT = ".ttf"

    private val fonts = mutableMapOf<Font, Typeface>()

    fun get(context: Context, font: Font): Typeface = fonts.getOrPut(font) {
        createTypeface(context, font)
    }

    private fun createTypeface(context: Context, font: Font): Typeface =
            Typeface.createFromAsset(context.resources.assets, "$DIR${font.fontName}$EXT")

    enum class Font(val fontName: String) {
        LIGHT("light"),
        REGULAR("usual"),
        MEDIUM("medium"),
        BOLD("bold"),
        BLACK("black")
    }
}
