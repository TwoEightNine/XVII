package com.twoeightnine.root.xvii.chats.attachments.gallery

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.provider.MediaStore
import com.twoeightnine.root.xvii.chats.attachments.base.BaseAttachViewModel
import com.twoeightnine.root.xvii.chats.attachments.gallery.model.GalleryItem
import com.twoeightnine.root.xvii.utils.applySingleSchedulers
import io.reactivex.Single

class GalleryViewModel(private val context: Context) : BaseAttachViewModel<GalleryItem>() {

    private val preloadedItems = arrayListOf<GalleryItem>()

    @SuppressLint("CheckResult")
    override fun loadAttach(offset: Int) {
        // full refresh
        if (offset == 0) {
            preloadedItems.clear()
        }
        Single.just(preloadedItems)
                .flatMap(::preloadMedia)
                .map { it.subList(offset, offset + COUNT) }
                .flatMap(::loadThumbnailsForItems)
                .compose(applySingleSchedulers())
                .subscribe({ photos ->
                    if (offset == 0) {
                        photos.add(0, GalleryAdapter.CAMERA_STUB)
                    }
                    onAttachmentsLoaded(offset, ArrayList(photos))
                }, { onErrorOccurred(it.message ?: "") })
    }

    private fun loadThumbnailsForItems(items: MutableList<GalleryItem>): Single<MutableList<GalleryItem>> {
        items.forEach { item ->
            item.thumbnail = when (item.type) {
                GalleryItem.Type.PHOTO -> {
                    BitmapFactory.decodeFile(item.path, BitmapFactory.Options().apply {
                        inSampleSize = getOptimalScaleForImage(item.path)
                    })
                }
                GalleryItem.Type.VIDEO -> {
                    ThumbnailUtils.createVideoThumbnail(item.path, MediaStore.Images.Thumbnails.MICRO_KIND)
                }
            }
        }
        return Single.just(items)
    }

    /**
     * loads photos and videos, sorts them by date descending, filter for non-empty paths
     */
    private fun preloadMedia(items: ArrayList<GalleryItem>): Single<ArrayList<GalleryItem>> {
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
    private fun loadAllVideos(): ArrayList<GalleryItem> {
        val videos = arrayListOf<GalleryItem>()
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
                    videos.add(GalleryItem(date, path, GalleryItem.Type.VIDEO, duration))
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
    private fun loadAllPhotos(): ArrayList<GalleryItem> {
        val photos = arrayListOf<GalleryItem>()
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
                    photos.add(GalleryItem(date, path, GalleryItem.Type.PHOTO))
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

    override fun onCleared() {
        super.onCleared()
        attachLiveData.value?.data?.forEach {  item ->
            item.thumbnail?.recycle()
            item.thumbnail = null
        }
    }

    companion object {
        const val COUNT = 30
        const val OPTIMAL_SIZE = 400
    }
}