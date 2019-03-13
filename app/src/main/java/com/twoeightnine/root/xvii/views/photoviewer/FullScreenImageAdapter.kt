package com.twoeightnine.root.xvii.views.photoviewer

import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import androidx.viewpager.widget.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.managers.Lg

class FullScreenImageAdapter(private val activity: Activity,
                             private val urls: ArrayList<String>,
                             private var dismissListener: (() -> Unit)?,
                             private var tapListener: (() -> Unit)?) : androidx.viewpager.widget.PagerAdapter() {

    private lateinit var inflater: LayoutInflater
    private val tivManager = ActiveTivManager(3)

    override fun getCount() = urls.size

    override fun isViewFromObject(view: View, any: Any) = view === any

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val viewLayout = inflater.inflate(R.layout.item_fullscreen_image, container, false)
        val imgDisplay = viewLayout.findViewById<TouchImageView>(R.id.tivImage)
        tivManager.saveTiv(position, imgDisplay)
        imgDisplay.dismissListener = dismissListener
        imgDisplay.tapListener = tapListener
        val url = urls[position]
        val fromFile = url.startsWith("file://")

        var requestCreator = Picasso.get()
                .load(urls[position])

        if (fromFile) {
            val size = getImageSize(url)
            var width = size.first
            var height = size.second
            val ratio = width.toFloat() / height.toFloat()
            if (width > height) {
                width = 3000
                height = (width / ratio).toInt()
            } else {
                height = 3000
                width = (ratio * height).toInt()
            }
            requestCreator = requestCreator
                    .resize(width, height)
                    .onlyScaleDown()
        }

        requestCreator
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .into(imgDisplay)

        container.addView(viewLayout)
        return viewLayout
    }

    fun canSlide(pos: Int) = tivManager.getTiv(pos)?.canSlide()

    private fun getImageSize(path: String): Pair<Int, Int> {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(path, options)
        val imageHeight = options.outHeight
        val imageWidth = options.outWidth
        return Pair(imageWidth, imageHeight)
    }

    override fun destroyItem(container: ViewGroup, position: Int, any: Any) {
        container.removeView(any as RelativeLayout)
    }
}
