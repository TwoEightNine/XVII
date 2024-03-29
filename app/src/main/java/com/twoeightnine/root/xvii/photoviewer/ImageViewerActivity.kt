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

package com.twoeightnine.root.xvii.photoviewer

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Size
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.FragmentPlacementActivity.Companion.startFragment
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.model.attachments.Photo
import com.twoeightnine.root.xvii.report.ReportFragment
import com.twoeightnine.root.xvii.utils.*
import global.msnthrp.xvii.uikit.extensions.*
import global.msnthrp.xvii.uikit.utils.DisplayUtils
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_image_viewer.*
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject


class ImageViewerActivity : AppCompatActivity() {

    private val photos = arrayListOf<Photo>()
    private val permissionHelper by lazy { PermissionHelper(this) }
    private val adapter by lazy {
        FullScreenImageAdapter(this, getUrlList(), ImageInteractionCallback(), getSizesOrNull())
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
        window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        DisplayUtils.initIfNot(this)
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


        ivBack.setOnClickListener { onBackPressed() }
        rlTop.applyTopInsetPadding()
        rlBottom.applyBottomInsetPadding()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    private fun initButtons() {
        btnDownload.setOnClickListener {
            if (photos.isEmpty()) return@setOnClickListener
            permissionHelper.doOrRequest(
                    arrayOf(PermissionHelper.WRITE_STORAGE, PermissionHelper.READ_STORAGE),
                    R.string.no_access_to_storage,
                    R.string.need_access_to_storage
            ) {
                val photo = currentPhoto() ?: return@doOrRequest
                val url = tryToGetUrl(photo) ?: return@doOrRequest

                var fileName = url.getUriName().toLowerCase()
                if ('?' in fileName) {
                    fileName = fileName.split('?')[0]
                }
                val file = File(SAVE_FILE, fileName)
                val downloadId = DownloadUtils.download(this, file, url)
                downloadingQueue[downloadId] = file.absolutePath
            }

        }
        btnSaveToAlbum.setOnClickListener {
            if (photos.isEmpty()) return@setOnClickListener

            val photo = currentPhoto() ?: return@setOnClickListener
            apiUtils.saveToAlbum(this, photo.ownerId, photo.id, photo.accessKey)
        }
        btnShare.setOnClickListener {
            if (photos.isEmpty()) return@setOnClickListener

            val photo = currentPhoto() ?: return@setOnClickListener
            shareImage(this, tryToGetUrl(photo))
        }
        btnReport.setOnClickListener {
            if (photos.isEmpty()) return@setOnClickListener

            reportCurrentPhoto()
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
        SimpleBitmapTarget { bitmap, _ ->

            var bmpUri: Uri? = null
            try {
                val file = File(context.externalCacheDir, "${System.currentTimeMillis()}.png")
                val out = FileOutputStream(file)
                bitmap?.compress(Bitmap.CompressFormat.PNG, 90, out)
                out.close()
                bmpUri = getUriForFile(context, file)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val i = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                putExtra(Intent.EXTRA_STREAM, bmpUri)
            }
            context.startActivity(Intent.createChooser(i, context.getString(R.string.share_image)))
        }.load(context, url)
    }

    private fun reportCurrentPhoto() {
        val photo = currentPhoto() ?: return

        val args = ReportFragment.createArgs(photo = photo)
        startFragment<ReportFragment>(args)
    }

    private fun getUrlList() = when (mode) {
        MODE_PHOTOS_LIST -> getUrlsFromPhotos(photos)
        MODE_ONE_PATH -> arrayListOf(filePath!!)
        else -> arrayListOf()
    }

    private fun getSizesOrNull(): List<Size>? = when (mode) {
        MODE_PHOTOS_LIST -> photos.mapNotNull { it.getMaxPhoto()?.run { Size(width, height) } }
        else -> null
    }

    private fun setPosition(position: Int) {
        tvPosition.text = "${position + 1}/${photos.size}"
        val currentPhoto = currentPhoto()
        if (mode == MODE_PHOTOS_LIST && currentPhoto != null) {
            val text = currentPhoto.text
            tvText.setVisible(!text.isNullOrEmpty())
            tvText.text = text
            tvDate.text = getTime(currentPhoto.date, withSeconds = Prefs.showSeconds)
        }
    }

    private fun currentPhoto(): Photo? = photos.getOrNull(vpImage.currentItem)

    private fun getUrlsFromPhotos(photos: ArrayList<Photo>) = ArrayList(photos.mapNotNull { tryToGetUrl(it) })

    private fun tryToGetUrl(photo: Photo) =
            photo.getMaxPhoto()?.url
                    ?: photo.getLargePhoto()?.url
                    ?: photo.getOptimalPhoto()?.url

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(actionDownloadedReceiver)
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

        fun viewImages(context: Context?, photos: List<Photo>, position: Int = 0) {
            context ?: return
            if (photos.isEmpty()) return

            val intent = Intent(context, ImageViewerActivity::class.java).apply {
                putParcelableArrayListExtra(PHOTOS, ArrayList(photos))
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

    private inner class ImageInteractionCallback : TouchImageView.InteractionCallback {

        override fun onTap() {
            if (mode == MODE_ONE_PATH) return
            rlControls.toggle()
        }

        override fun onDoubleTap() {
        }

        override fun onDismiss() {
            finish()
        }
    }

    private inner class ActionDownloadedReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.extras?.getLong(DownloadManager.EXTRA_DOWNLOAD_ID) ?: -1

            downloadingQueue[id]?.also { path ->
                val activity = this@ImageViewerActivity
                addToGallery(activity, path)
            }
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
