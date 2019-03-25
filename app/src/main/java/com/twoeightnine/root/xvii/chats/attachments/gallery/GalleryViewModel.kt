package com.twoeightnine.root.xvii.chats.attachments.gallery

import android.annotation.SuppressLint
import android.content.Context
import android.provider.MediaStore
import com.twoeightnine.root.xvii.chats.attachments.base.BaseAttachViewModel
import com.twoeightnine.root.xvii.lg.Lg
import com.twoeightnine.root.xvii.utils.applySingleSchedulers
import io.reactivex.Single

class GalleryViewModel(private val context: Context) : BaseAttachViewModel<String>() {

    @SuppressLint("CheckResult")
    override fun loadAttach(offset: Int) {
        if (offset != 0) {
            attachLiveData.value = attachLiveData.value
            return
        }

        Single.fromCallable { getPhotos() }
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
}