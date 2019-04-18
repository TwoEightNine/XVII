package com.twoeightnine.root.xvii.photoviewer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.model.attachments.Photo
import com.twoeightnine.root.xvii.utils.*
import kotlinx.android.synthetic.main.activity_image_viewer.*
import javax.inject.Inject

class ImageViewerActivity : AppCompatActivity() {

    private val photos = arrayListOf<Photo>()
    private val adapter by lazy {
        val urls = getUrlList()
        FullScreenImageAdapter(this, urls, ::onDismiss, ::onTap)
    }

    private var position: Int = 0
    private var filePath: String? = null
    private var mode = MODE_UNKNOWN

    @Inject
    lateinit var apiUtils: ApiUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_image_viewer)
        App.appComponent?.inject(this)
        initData()
        setPosition(position)

        vpImage.adapter = adapter
        vpImage.addOnPageChangeListener(ImageViewerPageListener())
        vpImage.currentItem = position
        initButtons()
        if (mode == MODE_ONE_PATH) {
            rlControls.hide()
        }
    }

    private fun initButtons() {
        btnDownload.setOnClickListener {
            if (photos.isEmpty()) return@setOnClickListener

            val url = photos[vpImage.currentItem].maxPhoto
            val fileName = getNameFromUrl(url)
            downloadFile(
                    this,
                    url,
                    fileName,
                    DownloadFileAsyncTask.PIC,
                    { showToast(this, getString(R.string.doenloaded, fileName)) }
            )
        }
        btnSaveToAlbum.setOnClickListener {
            if (photos.isEmpty()) return@setOnClickListener

            val photo = photos[vpImage.currentItem]
            apiUtils.saveToAlbum(this, photo.ownerId, photo.id, photo.accessKey)
        }
    }

    private fun initData() {
        intent.extras?.apply {
            mode = getInt(MODE, MODE_UNKNOWN)
            when (mode) {
                MODE_PHOTOS_LIST -> {
                    photos.addAll(getParcelableArrayList(PHOTOS) ?: arrayListOf())
                    position = getInt(POSITION)
                }
                MODE_ONE_PATH -> {
                    filePath = getString(PATH)
                }
                else -> finish()
            }
        }
    }

    private fun onDismiss() {
        finish()
    }

    private fun onTap() {
        if (mode == MODE_ONE_PATH) return

        rlControls.toggle()
    }

    private fun getUrlList() = when (mode) {
        MODE_PHOTOS_LIST -> getUrlsFromPhotos(photos)
        MODE_ONE_PATH -> arrayListOf(filePath!!)
        else -> arrayListOf()
    }

    private fun setPosition(position: Int) {
        tvPosition.text = "${position + 1}/${photos.size}"
    }

    private fun getUrlsFromPhotos(photos: ArrayList<Photo>) = ArrayList(photos.map { it.maxPhoto })

    companion object {

        const val PHOTOS = "urls"
        const val POSITION = "position"
        const val PATH = "path"
        const val MODE = "mode"

        const val MODE_UNKNOWN = 0
        const val MODE_PHOTOS_LIST = 1
        const val MODE_ONE_PATH = 2

        fun viewImages(context: Context?, photos: ArrayList<Photo>, position: Int = 0) {
            context ?: return
            if (photos.isEmpty()) return

            val intent = Intent(context, ImageViewerActivity::class.java).apply {
                putParcelableArrayListExtra(PHOTOS, photos)
                putExtra(POSITION, position)
                putExtra(MODE, MODE_PHOTOS_LIST)
            }
            context.startActivity(intent)
        }

        fun viewImage(context: Context?, filePath: String) {
            context ?: return

            val intent = Intent(context, ImageViewerActivity::class.java).apply {
                putExtra(PATH, filePath)
                putExtra(MODE, MODE_ONE_PATH)
            }
            context.startActivity(intent)
        }
    }

    private inner class ImageViewerPageListener : ViewPager.OnPageChangeListener {

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

        override fun onPageSelected(position: Int) {
            setPosition(position)
        }

        override fun onPageScrollStateChanged(state: Int) {}
    }

}
