package com.twoeightnine.root.xvii.fragments

import android.text.Html
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.squareup.picasso.Picasso
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.utils.screenWidth

/**
 * Created by chuck palahniuk on 8/18/17.
 */

class EggFragment: BaseFragment() {

    @BindView(R.id.ivBack)
    lateinit var ivBack: ImageView
    @BindView(R.id.tvText)
    lateinit var tvText: TextView

    override fun bindViews(view: View) {
        super.bindViews(view)
        ButterKnife.bind(this, view)
        val scale = 500f / 335f
        val width = screenWidth(activity)
        val height = (width / scale).toInt()
        Picasso.with(activity)
                .load("https://s-media-cache-ak0.pinimg.com/originals/22/6e/f8/226ef80405ed0da0b726c13d4a0bc9a1.jpg")
                .resize(width, height)
                .centerCrop()
                .into(ivBack)
        tvText.setText(Html.fromHtml(getString(R.string.quote)))

    }

    override fun getLayout() = R.layout.fragment_egg
}