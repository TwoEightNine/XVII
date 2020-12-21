package com.twoeightnine.root.xvii.uikit

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.databinding.ViewAvatarBinding
import com.twoeightnine.root.xvii.extensions.load
import global.msnthrp.xvii.uikit.extensions.hide
import global.msnthrp.xvii.uikit.extensions.show
import global.msnthrp.xvii.uikit.utils.color.AvatarColors

class XviiAvatar @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null)
    : RelativeLayout(context, attributeSet) {

    private var binding = ViewAvatarBinding.inflate(LayoutInflater.from(context), this)

    private val avatarNameSize: Int

    init {
        val attrs = context.theme.obtainStyledAttributes(attributeSet, R.styleable.XviiAvatar, 0, 0)
        avatarNameSize = attrs.getDimensionPixelSize(R.styleable.XviiAvatar_avatarNameSize, DEFAULT_NAME_SIZE)
        attrs.recycle()

        binding.tvAvatarName.setTextSize(TypedValue.COMPLEX_UNIT_PX, avatarNameSize.toFloat())
    }

    fun load(url: String?, text: String? = null, id: Int = 0) {
        when {
            url == null || EMPTY_AVATAR in url -> loadColor(text, id)
            else -> loadUrl(url)
        }
    }

    private fun loadColor(text: String? = null, id: Int = 0) {
        binding.civAvatarPhoto.setImageDrawable(ColorDrawable(AvatarColors.getColor(id)))
        text?.also(binding.tvAvatarName::setText)
        binding.tvAvatarName.show()
    }

    private fun loadUrl(url: String?) {
        binding.civAvatarPhoto.load(url)
        binding.tvAvatarName.hide()
    }

    companion object {
        private const val DEFAULT_NAME_SIZE = 30
        private const val EMPTY_AVATAR = "https://vk.com/images/camera_"
    }

}