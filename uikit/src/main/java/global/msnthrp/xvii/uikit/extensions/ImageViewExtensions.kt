package global.msnthrp.xvii.uikit.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition


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
    val placeholderIfNeeded = { rb: RequestBuilder<Drawable> ->
        if (placeholder) {
            val placeholderDrawable = ColorDrawable(placeholderColor)
            rb.placeholder(placeholderDrawable)
                    .error(placeholderDrawable)
        } else {
            rb
        }
    }
    Glide.with(this)
            .load(urlOrStub)
//            .let(placeholderIfNeeded)
            .block()
            .into(this)
}

fun SimpleBitmapTarget.load(
        context: Context,
        url: String,
        block: RequestBuilder<Bitmap>.() -> RequestBuilder<Bitmap> = { this }
) {
    Glide.with(context)
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