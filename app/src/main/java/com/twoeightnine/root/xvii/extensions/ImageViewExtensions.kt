/*
 * xvii - messenger for vk
 * Copyright (C) 2021  TwoEightNine
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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