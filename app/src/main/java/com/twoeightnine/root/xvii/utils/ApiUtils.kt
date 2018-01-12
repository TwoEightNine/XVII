package com.twoeightnine.root.xvii.utils

import android.content.Context
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.activities.VideoViewerActivity
import com.twoeightnine.root.xvii.consts.Api
import com.twoeightnine.root.xvii.dagger.ApiService
import com.twoeightnine.root.xvii.managers.Lg
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.managers.Session
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.model.Video
import com.twoeightnine.root.xvii.views.photoviewer.ImageViewerActivity
import java.util.*
import javax.inject.Inject

class ApiUtils @Inject constructor(val api: ApiService) {

    private val stickersUpdPeriod = 60 * 60 * 24 * 3 //3 days

    fun setOffline() {
        api.setOffline()
                .subscribeSmart({}, {})
    }

    fun setActivity(userId: Int) {
        api.setActivity(userId)
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
                    Session.fullName = user.fullName()
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
                        ImageViewerActivity.viewImages(context, response)
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
                    showCommon(context, R.string.added_to_saved)
                }, {
                    showError(context, it)
                })
    }

    fun saveDoc(context: Context, ownerId: Int, docId: Int, accessKey: String) {
        api.addDoc(ownerId, docId, accessKey)
                .subscribeSmart({
                    showCommon(context, R.string.added_to_docs)
                }, {
                    showError(context, it)
                })
    }

    fun checkMembership(callback: (Boolean) -> Unit) {
        api.isGroupMember(Api.GROUP, Session.uid)
                .subscribeSmart({
                    callback.invoke(it == 1)
                }, {
                    error ->
                    Lg.wtf("check membership error: $error")
                })
    }

    fun joinGroup() {
        api.joinGroup(Api.GROUP)
                .subscribeSmart({}, {})
    }

    fun shareToWall(ownerId: Int, message: String, attachments: String = "",
                    onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        api.postToWall(ownerId, message, attachments)
                .subscribeSmart({
                    onSuccess.invoke()
                }, onError)
    }

    fun updateStickers() {
        if (time() - Prefs.lastStickersUpdate < stickersUpdPeriod) return

        api.getStickers()
                .subscribeSmart({
                    response ->
                    val ids = mutableListOf<Int>()
                    response.dictionary?.forEach {
                        it.userStickers?.forEach {
                            ids.add(it)
                        }
                    }
                    Collections.sort(ids)
                    Prefs.availableStickers = ids
                    Prefs.lastStickersUpdate = time()
                }, {})
    }

    fun trackVisitor(onSuccess: () -> Unit = {}) {
        api.trackVisitor()
                .subscribeSmart({
                    onSuccess.invoke()
                }, {})

    }
}
