package com.twoeightnine.root.xvii.extensions

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.bumptech.glide.RequestBuilder
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.utils.ColorManager
import global.msnthrp.xvii.uikit.extensions.load

fun ImageView.load(url: String?, placeholder: Boolean = true,
                   block: RequestBuilder<Drawable>.() -> RequestBuilder<Drawable> = { this }) {
    val stubColorUrl = ColorManager.getPhotoStub()
    val placeholderColor = ContextCompat.getColor(context, R.color.placeholder)
    load(url, stubColorUrl, placeholderColor, placeholder, block)
}