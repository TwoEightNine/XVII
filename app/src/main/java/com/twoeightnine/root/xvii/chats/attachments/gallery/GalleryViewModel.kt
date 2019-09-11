package com.twoeightnine.root.xvii.chats.attachments.gallery

import android.annotation.SuppressLint
import android.content.Context
import android.provider.MediaStore
import com.twoeightnine.root.xvii.chats.attachments.base.BaseAttachViewModel
import com.twoeightnine.root.xvii.chats.attachments.gallery.model.GalleryItem
import com.twoeightnine.root.xvii.lg.Lg
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.utils.applySingleSchedulers
import io.reactivex.Single

class GalleryViewModel(private val context: Context) : BaseAttachViewModel<GalleryItem>() {

    @SuppressLint("CheckResult")
    override fun loadAttach(offset: Int) {
        if (offset != 0) {
            attachLiveData.value = Wrapper(attachLiveData.value?.data ?: return)
            return
        }

        Single.fromCallable { getMedia() }
                .compose(applySingleSchedulers())
                .subscribe({ photos ->
                    photos.add(0, GalleryAdapter.CAMERA_STUB)
                    onAttachmentsLoaded(offset, photos)
                }, { onErrorOccurred(it.message ?: "") })
    }

    private fun getPhotos(): ArrayList<String> {
        val result = arrayListOf<String>()
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.MediaColumns.DATE_MODIFIED)
        val cursor = context.contentResolver.query(
                uri, projection, null, null,
                "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"
        ) ?: return result
        val columnIndexData = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)

        var corrupted = 0
        while (cursor.moveToNext()) {
            val path = cursor.getString(columnIndexData)
            if (path != null) {
                result.add(path)
            } else {
                corrupted++
            }
        }
        if (corrupted > 1) {
            Lg.wtf("[gallery] $corrupted corrupted images")
        }
        cursor.close()
        return result
    }

    private fun getMedia(): ArrayList<GalleryItem> {
        val result = arrayListOf<GalleryItem>()

        val projectionImages = arrayOf(MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DATE_MODIFIED)
        val cursorImages = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projectionImages, null, null,
                "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"
        ) ?: return result
        try {
            cursorImages.moveToFirst()
            do {
                val date = cursorImages.getLong(cursorImages.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED))
                val path = cursorImages.getString(cursorImages.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
                if (path != null) {
                    result.add(GalleryItem(date, path, GalleryItem.Type.PHOTO))
                }
            } while (cursorImages.moveToNext())

            cursorImages.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val projectionVideos = arrayOf(MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DATE_MODIFIED, MediaStore.Video.VideoColumns.DURATION)
        val cursorVideos = context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projectionVideos, null, null,
                "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"
        ) ?: return result
        try {
            cursorVideos.moveToFirst()
            do {
                val date = cursorVideos.getLong(cursorVideos.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED))
                val path = cursorVideos.getString(cursorVideos.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
                val duration = cursorVideos.getLong(cursorVideos.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DURATION))
                if (path != null) {
                    result.add(GalleryItem(date, path, GalleryItem.Type.VIDEO, duration))
                }
            } while (cursorVideos.moveToNext())

            cursorVideos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        result.sortByDescending { it.date }
        result.filter { it.path.isNotEmpty() }
        return result
    }
}