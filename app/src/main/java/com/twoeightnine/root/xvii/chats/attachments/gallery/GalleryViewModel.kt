package com.twoeightnine.root.xvii.chats.attachments.gallery

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.provider.MediaStore
import com.twoeightnine.root.xvii.chats.attachments.base.BaseAttachViewModel
import com.twoeightnine.root.xvii.chats.attachments.gallery.model.DeviceItem
import com.twoeightnine.root.xvii.crypto.md5
import com.twoeightnine.root.xvii.utils.applySingleSchedulers
import com.twoeightnine.root.xvii.utils.saveBmp
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import java.io.File
import kotlin.math.min

class GalleryViewModel(private val context: Context) : BaseAttachViewModel<DeviceItem>() {

    private var disposable: Disposable? = null
    private val preloadedItems = arrayListOf<DeviceItem>()

    init {
        File(context.cacheDir, CACHE_DIR).mkdir()
    }

    @SuppressLint("CheckResult")
    override fun loadAttach(offset: Int) {
        disposable?.dispose()
        // full refresh
        if (offset == 0) {
            preloadedItems.clear()
        }
        disposable = Single.just(preloadedItems)
                .flatMap(::preloadMedia)
                .map {
                    if (offset >= it.size) {
                        arrayListOf()
                    } else {
                        it.subList(offset, min(offset + COUNT, it.size))
                    }
                }
                .flatMap(::loadThumbnailsForItems)
                .compose(applySingleSchedulers())
                .subscribe({ photos ->
                    onAttachmentsLoaded(offset, ArrayList(photos))
                }, { onErrorOccurred(it.message ?: "") })
    }

    private fun loadThumbnailsForItems(items: MutableList<DeviceItem>): Single<MutableList<DeviceItem>> {
        items.forEach { item ->
            val cachedThumbnail = getThumbnail(item)
            item.thumbnail = cachedThumbnail ?: when (item.type) {
                DeviceItem.Type.VIDEO -> {
                    ThumbnailUtils.createVideoThumbnail(item.path, MediaStore.Images.Thumbnails.MICRO_KIND)
                }
                else -> {
                    BitmapFactory.decodeFile(item.path, BitmapFactory.Options().apply {
                        inSampleSize = getOptimalScaleForImage(item.path)
                    })
                }
            }
            cachedThumbnail ?: saveThumbnailToCache(item)
        }
        return Single.just(items)
    }

    private fun getThumbnail(item: DeviceItem): Bitmap? {
        val fileName = getFilePathForItem(item)
        return BitmapFactory.decodeFile(fileName)

    }

    private fun saveThumbnailToCache(item: DeviceItem) {
        val bmp = item.thumbnail ?: return
        val fileName = getFilePathForItem(item)
        saveBmp(fileName, bmp)
    }

    /**
     * loads photos and videos, sorts them by date descending, filter for non-empty paths
     */
    private fun preloadMedia(items: ArrayList<DeviceItem>): Single<ArrayList<DeviceItem>> {
        if (items.isEmpty()) {
            items.addAll(loadAllPhotos())
            items.addAll(loadAllVideos())
            items.sortByDescending { it.date }
            items.filter { it.path.isNotEmpty() }
        }
        return Single.just(items)
    }

    /**
     * load all videos from gallery
     */
    private fun loadAllVideos(): ArrayList<DeviceItem> {
        val videos = arrayListOf<DeviceItem>()
        val projectionVideos = arrayOf(MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DATE_MODIFIED, MediaStore.Video.VideoColumns.DURATION)

        var cursorVideos: Cursor? = null
        try {
            cursorVideos = context.contentResolver.query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    projectionVideos, null, null,
                    "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"
            )
            cursorVideos?.moveToFirst()
            do {
                val date = cursorVideos.getLong(cursorVideos.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED))
                val path = cursorVideos.getString(cursorVideos.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
                val duration = cursorVideos.getLong(cursorVideos.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DURATION))
                if (path != null) {
                    videos.add(DeviceItem(date, path, DeviceItem.Type.VIDEO, duration))
                }
            } while (cursorVideos.moveToNext())

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursorVideos?.close()
        }
        return videos
    }

    /**
     * loads all photos from gallery
     */
    private fun loadAllPhotos(): ArrayList<DeviceItem> {
        val photos = arrayListOf<DeviceItem>()
        val projectionImages = arrayOf(MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DATE_MODIFIED)

        var cursorImages: Cursor? = null
        try {
            cursorImages = context.contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projectionImages, null, null,
                    "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"
            )
            cursorImages?.moveToFirst()
            do {
                val date = cursorImages.getLong(cursorImages.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED))
                val path = cursorImages.getString(cursorImages.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
                if (path != null) {
                    photos.add(DeviceItem(date, path, DeviceItem.Type.PHOTO))
                }
            } while (cursorImages.moveToNext())

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursorImages?.close()
        }
        return photos
    }

    private fun getOptimalScaleForImage(path: String): Int {
        val optionsBounds = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(path, optionsBounds)
        var width = optionsBounds.outWidth
        var height = optionsBounds.outHeight
        var scale = 1
        while (width > OPTIMAL_SIZE && height > OPTIMAL_SIZE) {
            width /= 2
            height /= 2
            scale *= 2
        }
        return scale
    }

    private fun getFilePathForItem(item: DeviceItem): String {
        val cacheDir = File(context.cacheDir, CACHE_DIR)
        return File(cacheDir, md5(item.path) + ".png").absolutePath
    }

    override fun onCleared() {
        super.onCleared()
        attachLiveData.value?.data?.forEach { item ->
            item.thumbnail?.recycle()
            item.thumbnail = null
        }
    }

    companion object {
        private const val CACHE_DIR = "gallery_cache"

        const val COUNT = 30
        const val OPTIMAL_SIZE = 400
    }
}