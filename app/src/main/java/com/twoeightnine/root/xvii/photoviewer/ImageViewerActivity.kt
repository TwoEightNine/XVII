package com.twoeightnine.root.xvii.photoviewer

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.model.attachments.Photo
import com.twoeightnine.root.xvii.utils.*
import kotlinx.android.synthetic.main.activity_image_viewer.*
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject


class ImageViewerActivity : AppCompatActivity() {

    private val photos = arrayListOf<Photo>()
    private val permissionHelper by lazy { PermissionHelper(this) }
    private val adapter by lazy {
        FullScreenImageAdapter(this, getUrlList(), ::onDismiss, ::onTap)
    }

    private val downloadingQueue = hashMapOf<Long, String>()
    private val actionDownloadedReceiver = ActionDownloadedReceiver()

    private var position: Int = 0
    private var filePath: String? = null
    private var mode = MODE_UNKNOWN

    @Inject
    lateinit var apiUtils: ApiUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.BLACK
//        requestWindowFeature(Window.FEATURE_NO_TITLE)
//        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_image_viewer)
        App.appComponent?.inject(this)
        initData()
        setPosition(position)
        with(vpImage) {
            adapter = this@ImageViewerActivity.adapter
            addOnPageChangeListener(ImageViewerPageListener())
            pageMargin = 30
            setPageTransformer(false, ImagePageTransformer())
            currentItem = position
        }
        initButtons()
        if (mode == MODE_ONE_PATH) {
            rlControls.hide()
        }
        registerReceiver(actionDownloadedReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    private fun initButtons() {
        btnDownload.setOnClickListener {
            if (photos.isEmpty()) return@setOnClickListener
            permissionHelper.doOrRequest(
                    arrayOf(PermissionHelper.WRITE_STORAGE, PermissionHelper.READ_STORAGE),
                    R.string.no_access_to_storage,
                    R.string.need_access_to_storage
            ) {
                val url = tryToGetUrl(currentPhoto()) ?: return@doOrRequest

                val fileName = getNameFromUrl(url).toLowerCase()
                val file = File(SAVE_FILE, fileName)

                val request = DownloadManager.Request(Uri.parse(url))
                        .setTitle(fileName)
                        .setDescription(getString(R.string.download))
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        .setDestinationUri(Uri.fromFile(file))
                        .setAllowedOverMetered(true)
                        .setAllowedOverRoaming(true)

                val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val downloadId = downloadManager.enqueue(request)
                downloadingQueue[downloadId] = file.absolutePath
            }

        }
        btnSaveToAlbum.setOnClickListener {
            if (photos.isEmpty()) return@setOnClickListener

            val photo = currentPhoto()
            apiUtils.saveToAlbum(this, photo.ownerId, photo.id, photo.accessKey)
        }
        btnShare.setOnClickListener {
            if (photos.isEmpty()) return@setOnClickListener

            shareImage(this, tryToGetUrl(currentPhoto()))
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

    private fun shareImage(context: Context?, url: String?) {
        if (context == null || url == null) return

        Picasso.get().load(url).into(object : Target {
            override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                val i = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    putExtra(Intent.EXTRA_STREAM, saveBitmap(bitmap))
                }
                context.startActivity(Intent.createChooser(i, context.getString(R.string.share_image)))
            }

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}

            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}

            private fun saveBitmap(bmp: Bitmap): Uri? {
                var bmpUri: Uri? = null
                try {
                    val file = File(context.externalCacheDir, "${System.currentTimeMillis()}.png")
                    val out = FileOutputStream(file)
                    bmp.compress(Bitmap.CompressFormat.PNG, 90, out)
                    out.close()
                    bmpUri = getUriForFile(context, file)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return bmpUri
            }

        })
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
        if (mode == MODE_PHOTOS_LIST) {
            val text = currentPhoto().text
            tvText.setVisible(!text.isNullOrEmpty())
            tvText.text = text
            tvDate.text = getTime(currentPhoto().date, withSeconds = Prefs.showSeconds)
        }
    }

    private fun currentPhoto() = photos[vpImage.currentItem]

    private fun getUrlsFromPhotos(photos: ArrayList<Photo>) = ArrayList(photos.mapNotNull { tryToGetUrl(it) })

    private fun tryToGetUrl(photo: Photo) =
            photo.getMaxPhoto()?.url
                    ?: photo.getLargePhoto()?.url
                    ?: photo.getOptimalPhoto()?.url

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(actionDownloadedReceiver)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.open_activity, R.anim.close_activity)
    }

    companion object {

        private const val SAVE_DIR = "vk"
        val SAVE_FILE = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), SAVE_DIR)

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

    private inner class ActionDownloadedReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.extras?.getLong(DownloadManager.EXTRA_DOWNLOAD_ID) ?: -1

            downloadingQueue[id]?.also { path ->
                val activity = this@ImageViewerActivity
                addToGallery(activity, path)
                showToast(activity, getString(R.string.doenloaded, getNameFromUrl(path)))
            }
        }
    }

}
