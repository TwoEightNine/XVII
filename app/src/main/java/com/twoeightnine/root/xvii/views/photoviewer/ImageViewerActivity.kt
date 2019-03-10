package com.twoeightnine.root.xvii.views.photoviewer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.managers.Lg
import com.twoeightnine.root.xvii.model.Photo
import com.twoeightnine.root.xvii.utils.*
import javax.inject.Inject

class ImageViewerActivity : AppCompatActivity() {

    @BindView(R.id.rlTop)
    lateinit var rlTop: RelativeLayout
    @BindView(R.id.tvPosition)
    lateinit var tvPosition: TextView
    @BindView(R.id.vpImage)
    lateinit var viewPager: StopableViewPager
    @BindView(R.id.llBottom)
    lateinit var llBottom: LinearLayout
    @BindView(R.id.btnSaveToAlbum)
    lateinit var btnSave: Button
    @BindView(R.id.btnDownload)
    lateinit var btnDownload: Button

    private var adapter: FullScreenImageAdapter? = null
    private var photos: MutableList<Photo>? = null
    private var filePath: String? = null
    private var position: Int = 0
    private var fileMode = false

    @Inject
    lateinit var apiUtils: ApiUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_image_viewer)
        ButterKnife.bind(this)
        App.appComponent?.inject(this)
        initData()
        val urls = when{
            photos != null -> getUrlsFromPhotos(photos!!)
            filePath != null -> arrayListOf(filePath!!)
            else -> arrayListOf()
        }
        adapter = FullScreenImageAdapter(this, urls, ::onDismiss, ::onTap)
        viewPager.adapter = adapter
        setPosition(position)
        viewPager.addOnPageChangeListener(object : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                setPosition(position)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        viewPager.currentItem = position
        btnDownload.setOnClickListener {
            if (photos == null || photos!!.size == 0) return@setOnClickListener

            val url = photos!![viewPager.currentItem].maxPhoto
            val fileName = getNameFromUrl(url)
            downloadFile(
                    this,
                    url,
                    fileName,
                    DownloadFileAsyncTask.PIC,
                    { showCommon(this, getString(R.string.doenloaded, fileName)) }
            )
        }
        btnSave.setOnClickListener {
            if (photos == null || photos!!.size == 0) return@setOnClickListener

            val photo = photos!![viewPager.currentItem]
            Lg.i("photo: ${photo.ownerId} ${photo.id} ${photo.accessKey}")
            apiUtils.saveToAlbum(this, photo.ownerId ?: 0, photo.id ?: 0, photo.accessKey ?: "")
        }
        if (fileMode) {
            onTap()
        }
    }

    private fun initData() {
        val photosRaw = intent.getSerializableExtra(PHOTOS)
        if (photosRaw != null) {
            photos = (photosRaw as Array<Parcelable>)
                    .map { it as Photo }
                    .toMutableList()
        } else {
            fileMode = true
        }
        filePath = intent.getStringExtra(PATH)
        position = intent.getIntExtra(POSITION, 0)
    }

    private fun onDismiss() {
        finish()
    }

    private fun onTap() {
        visibilitor(rlTop)
        visibilitor(llBottom)
    }

    private fun setPosition(position: Int) {
        tvPosition.text = "${position + 1}/${photos?.size ?: 1}"
    }

    private fun getUrlsFromPhotos(photos: MutableList<Photo>) = photos
            .map { it.almostMax }
            .toMutableList()

    private fun visibilitor(vg: ViewGroup) {
        vg.visibility = if (vg.visibility == View.VISIBLE || fileMode) View.INVISIBLE else View.VISIBLE
    }

    companion object {

        const val PHOTOS = "urls"
        const val POSITION = "position"
        const val PATH = "path"

        fun viewImages(context: Context, photos: MutableList<Photo>, position: Int) {
            val intent = Intent(context, ImageViewerActivity::class.java)
            intent.putExtra(PHOTOS, photos.toTypedArray())
            intent.putExtra(POSITION, position)
            context.startActivity(intent)
        }

        fun viewImages(context: Context, urls: MutableList<Photo>) {
            viewImages(context, urls, 0)
        }

        fun viewImage(context: Context, filePath: String) {
            val intent = Intent(context, ImageViewerActivity::class.java)
            intent.putExtra(PATH, filePath)
            context.startActivity(intent)
        }
    }

}
