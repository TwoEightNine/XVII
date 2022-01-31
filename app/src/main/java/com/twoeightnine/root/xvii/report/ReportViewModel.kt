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

package com.twoeightnine.root.xvii.report

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.twoeightnine.root.xvii.base.BaseViewModel
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.model.WallPost
import com.twoeightnine.root.xvii.model.attachments.Photo
import global.msnthrp.xvii.core.report.ReportUseCase
import global.msnthrp.xvii.core.report.model.ReportReason
import global.msnthrp.xvii.data.report.NetworkReportDataSource

class ReportViewModel : BaseViewModel() {

    val loading: LiveData<Boolean>
        get() = loadingLiveData

    val sent: LiveData<Boolean>
        get() = sentLiveData

    private val reportUseCase = ReportUseCase(
            NetworkReportDataSource()
    )

    private val loadingLiveData = MutableLiveData(false)
    private val sentLiveData = MutableLiveData(false)

    fun reportUser(user: User, reason: ReportReason, comment: String) {
        loadingLiveData.postValue(true)
        onIoThread({
            reportUseCase.reportUser(user.id, reason, comment)
        }, ::onReported)
    }

    fun reportWallPost(wallPost: WallPost, reason: ReportReason) {
        loadingLiveData.postValue(true)
        onIoThread({
            reportUseCase.reportWallPost(wallPost.ownerId, wallPost.id, reason)
        }, ::onReported)
    }

    fun reportPhoto(photo: Photo, reason: ReportReason) {
        loadingLiveData.postValue(true)
        onIoThread({
            reportUseCase.reportPhoto(photo.ownerId, photo.id, reason)
        }, ::onReported)
    }

    override fun onErrorOccurred(throwable: Throwable?) {
        super.onErrorOccurred(throwable)
        loadingLiveData.postValue(false)
    }

    private fun onReported(unit: Unit) {
        loadingLiveData.postValue(false)
        sentLiveData.postValue(true)
    }

}