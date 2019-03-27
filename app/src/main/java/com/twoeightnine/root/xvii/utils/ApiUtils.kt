package com.twoeightnine.root.xvii.utils

import android.content.Context
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.activities.VideoViewerActivity
import com.twoeightnine.root.xvii.lg.Lg
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.managers.Session
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.model.Video
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.photoviewer.ImageViewerActivity
import javax.inject.Inject

class ApiUtils @Inject constructor(val api: ApiService) {

    companion object {
        private const val STICKERS_UPD_DELAY = 60 * 60 * 12 // 12 hours

        const val ACTIVITY_TYPING = "typing"
        const val ACTIVITY_VOICE = "audiomessage"
    }

    fun setOffline() {
        api.setOffline()
                .subscribeSmart({}, {})
    }

    fun setActivity(userId: Int, type: String = ACTIVITY_TYPING) {
        api.setActivity(userId, type)
                .subscribeSmart({}, {})
    }

    fun markAsRead(messageIds: String) {
        api.markAsRead(messageIds)
                .subscribeSmart({}, {})
    }

    fun checkAccount(token: String, uid: Int, success: () -> Unit, fail: (String) -> Unit, later: (String) -> Unit) {
        api.getUsers("$uid", User.FIELDS)
                .subscribeSmart({
                    response ->
                    val user = response[0]
                    Session.token = token
                    Session.uid = uid
                    Session.fullName = user.fullName
                    Session.photo = user.photo100 ?: "errrr"
                    success.invoke()
                }, {
                    error ->
                    Lg.wtf("check acc error: $error")
                    fail.invoke(error)
                }, {
                    Lg.wtf("check acc error: $it")
                    later.invoke(it)
                })
    }

    fun showPhoto(context: Context, photoId: String, accessKey: String?) {
        api.getPhotoById(photoId, accessKey ?: "")
                .subscribeSmart({
                    response ->
                    if (response.size == 0) {
                        showError(context, R.string.denied)
                    } else {
                        response.forEach {
                            it.accessKey = accessKey ?: ""
                        }
                        ImageViewerActivity.viewImages(context, ArrayList(response))
                    }
                }, {
                    error ->
                    showError(context, error)
                })
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
                        VideoViewerActivity.launch(context, response.items[0].player ?: "")
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

    fun checkMembership(callback: (Boolean) -> Unit) {
        api.isGroupMember(App.GROUP, Session.uid)
                .subscribeSmart({
                    callback.invoke(it == 1)
                }, {
                    error ->
                    Lg.wtf("check membership error: $error")
                })
    }

    fun joinGroup() {
        api.joinGroup(App.GROUP)
                .subscribeSmart({}, {})
    }

    fun repost(obj: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        api.repost(obj)
                .subscribeSmart({
                    onSuccess.invoke()
                }, onError)
    }

    fun updateStickers() {
        if (time() - Prefs.lastStickersUpdate < STICKERS_UPD_DELAY) return

        api.getStickers()
                .subscribeSmart({
                    response ->
                    val ids = mutableListOf<Int>()
                    response.dictionary?.forEach {
                        it.userStickers?.forEach {
                            ids.add(it)
                        }
                    }
                    ids.sort()
                    Prefs.availableStickers = ids.distinct().toMutableList()
                    Prefs.lastStickersUpdate = time()
                }, {})
    }

    fun trackVisitor(onSuccess: () -> Unit = {}) {
        api.trackVisitor()
                .subscribeSmart({
                    onSuccess.invoke()
                }, {})

    }

    fun downloadFile(url: String,
                     fileName: String,
                     onSuccess: (String) -> Unit = {},
                     onError: (String) -> Unit = {}) {
        api.downloadFile(url)
                .compose(applySchedulers())
                .subscribe({
                    val written = writeResponseBodyToDisk(it, fileName)
                    if (written) {
                        onSuccess.invoke(fileName)
                    } else {
                        onError.invoke("Error downloading $fileName: not written")
                    }
                }, {
                    it.printStackTrace()
                    val errorMsg = it.message ?: "download file error: null error"
                    Lg.wtf(errorMsg)
                    onError.invoke(errorMsg)
                })
    }
}
