package com.twoeightnine.root.xvii.utils

import android.content.Context
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.model.attachments.Video
import com.twoeightnine.root.xvii.network.ApiService
import javax.inject.Inject

@Deprecated("Move api calls into ViewModel")
class ApiUtils @Inject constructor(val api: ApiService) {

    fun markAsRead(messageIds: String) {
        api.markAsRead(messageIds)
                .subscribeSmart({}, {})
    }

    fun openVideo(context: Context, video: Video) {
        api.getVideos(
                video.videoId,
                video.accessKey ?: "",
                1, 0
                )
                .subscribeSmart({
                    response ->
                    if (response.items.size > 0 && response.items[0].player != null) {
                        UrlUtils.openUrl(context, response.items[0].player)
                    } else {
                        showError(context, context.getString(R.string.not_playable_video))
                    }
                }, {
                    error ->
                    showError(context, error)
                })
    }

    fun saveToAlbum(context: Context, ownerId: Int, photoId: Int, accessKey: String) {
        api.copyPhoto(ownerId, photoId, accessKey)
                .subscribeSmart({
                    showToast(context, R.string.added_to_saved)
                }, {
                    showError(context, it)
                })
    }

    fun saveDoc(context: Context, ownerId: Int, docId: Int, accessKey: String) {
        api.addDoc(ownerId, docId, accessKey)
                .subscribeSmart({
                    showToast(context, R.string.added_to_docs)
                }, {
                    showError(context, it)
                })
    }

    fun trackVisitor(onSuccess: () -> Unit = {}) {
        api.trackVisitor()
                .subscribeSmart({
                    onSuccess.invoke()
                }, {})

    }
}
