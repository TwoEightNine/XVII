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

package global.msnthrp.xvii.uikit.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import global.msnthrp.xvii.uikit.utils.GlideApp


fun ImageView.load(
        url: String?,
        stubColorUrl: String,
        placeholderColor: Int,
        placeholder: Boolean = true,
        block: RequestBuilder<Drawable>.() -> RequestBuilder<Drawable> = { this }
) {
    val urlOrStub = when {
        url.isNullOrBlank() -> stubColorUrl
        else -> url
    }
    if (context.isValidForGlide()) {
        GlideApp.with(this)
                .load(urlOrStub)
                .block()
                .into(this)
    }
}

fun SimpleBitmapTarget.load(
        context: Context,
        url: String,
        block: RequestBuilder<Bitmap>.() -> RequestBuilder<Bitmap> = { this }
) {
    GlideApp.with(context)
            .asBitmap()
            .load(url)
            .block()
            .into(this)
}

open class SimpleBitmapTarget(
        val tag: String = "bitmap target",
        private val result: (Bitmap?, Exception?) -> Unit
) : CustomTarget<Bitmap>() {

    override fun onLoadFailed(errorDrawable: Drawable?) {
        super.onLoadFailed(errorDrawable)
        result(null, Exception())
    }

    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
        result(resource, null)
    }

    override fun onLoadCleared(placeholder: Drawable?) {
    }
}