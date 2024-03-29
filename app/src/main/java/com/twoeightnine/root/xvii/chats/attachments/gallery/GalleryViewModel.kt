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

package com.twoeightnine.root.xvii.chats.attachments.gallery

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.provider.MediaStore
import com.twoeightnine.root.xvii.chats.attachments.base.BaseAttachViewModel
import com.twoeightnine.root.xvii.chats.attachments.gallery.model.DeviceItem
import com.twoeightnine.root.xvii.crypto.md5
import com.twoeightnine.root.xvii.lg.L
import com.twoeightnine.root.xvii.utils.CacheFileUtils
import com.twoeightnine.root.xvii.utils.applySingleSchedulers
import com.twoeightnine.root.xvii.utils.saveBmp
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.min

class GalleryViewModel(private val context: Context) : BaseAttachViewModel<DeviceItem>() {

    private var disposable: Disposable? = null
    private val preloadedItems = Collections.synchronizedList(mutableListOf<DeviceItem>())

    /**
     * to change behavior
     */
    var onlyPhotos = false

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
                }, { throwable ->
                    L.tag(TAG).throwable(throwable)
                            .log("error loading items")
                    onErrorOccurred(throwable.message ?: "")
                })
    }

    private fun loadThumbnailsForItems(
            items: MutableList<DeviceItem>
    ): Single<MutableList<DeviceItem>> =
            Single.fromCallable {
                items.forEach { item ->

                    item.thumbnail = when (item.type) {
                        DeviceItem.Type.VIDEO -> {

                            // get thumbnail's file and path
                            val thumbnailFile = getFileForItem(item)
                            val thumbnail = thumbnailFile.absolutePath

                            // file does not exist. create it!
                            if (!thumbnailFile.exists()) {

                                // create bitmap thumbnail and save it to path
                                val bitmap = ThumbnailUtils.createVideoThumbnail(item.path, MediaStore.Images.Thumbnails.MICRO_KIND)
                                if (bitmap != null) {
                                    saveBmp(thumbnail, bitmap)
                                } else {
                                    L.tag(TAG).warn()
                                            .log("video thumbnail is null")
                                }
                            }

                            // set path
                            "file://$thumbnail"
                        }
                        else -> {
                            // thumb for photos is the same photo. will be cropped before showing
                            "file://${item.path}"
                        }
                    }
                }
                items
            }

    /**
     * loads photos and videos, sorts them by date descending, filter for non-empty paths
     */
    private fun preloadMedia(items: MutableList<DeviceItem>): Single<MutableList<DeviceItem>> =
            Single.fromCallable {
                if (items.isEmpty()) {
                    items.addAll(loadAllPhotos())
                    if (!onlyPhotos) {
                        items.addAll(loadAllVideos())
                    }
                    items.sortByDescending { it.date }
                    items.filter { it.path.isNotEmpty() }
                }
                items
            }

    /**
     * load all videos from gallery
     */
    private fun loadAllVideos(): ArrayList<DeviceItem> {
        val videos = arrayListOf<DeviceItem>()
        val projectionVideos = arrayOf(MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DATE_MODIFIED, MediaStore.Video.VideoColumns.DURATION)

        try {
            context.contentResolver.query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    projectionVideos, null, null,
                    "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"
            )?.use { cursor ->
                cursor.moveToFirst()
                do {
                    val date = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED))
                    val path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
                    val duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DURATION))
                    if (path != null) {
                        videos.add(DeviceItem(date, path, DeviceItem.Type.VIDEO, duration))
                    }
                } while (cursor.moveToNext())
            }

        } catch (e: Exception) {
            L.tag(TAG)
                    .throwable(e)
                    .log("error loading videos")
        }
        return videos
    }

    /**
     * loads all photos from gallery
     */
    private fun loadAllPhotos(): ArrayList<DeviceItem> {
        val photos = arrayListOf<DeviceItem>()
        val projectionImages = arrayOf(
                MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.DATE_MODIFIED,
                MediaStore.MediaColumns.WIDTH,
                MediaStore.MediaColumns.HEIGHT
        )

        try {
            context.contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projectionImages, null, null,
                    "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"
            )?.use { cursor ->

                cursor.moveToFirst()
                do {
                    val date = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED))
                    val path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
                    if (path != null) {
                        photos.add(DeviceItem(date, path, DeviceItem.Type.PHOTO))
                    }
                } while (cursor.moveToNext())
            }

        } catch (e: Exception) {
            L.tag(TAG)
                    .throwable(e)
                    .log("error loading photos")
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

    private fun getFileForItem(item: DeviceItem): File {
        val cacheDir = File(context.cacheDir, CACHE_DIR)
        return File(cacheDir, md5(item.path) + ".png")
    }

    companion object {
        private const val TAG = "gallery"
        private const val CACHE_DIR = CacheFileUtils.DIR_GALLERY

        const val COUNT = 30
        const val OPTIMAL_SIZE = 400
    }
}