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

package com.twoeightnine.root.xvii.analyzer.dialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.model.WrappedLiveData
import com.twoeightnine.root.xvii.model.WrappedMutableLiveData
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.utils.subscribeSmart
import javax.inject.Inject

class AnalyseDialogViewModel(private val api: ApiService) : ViewModel() {

    private val userLiveData = WrappedMutableLiveData<User>()
    private val progressLiveData = WrappedMutableLiveData<Pair<Int, Int>>()

    var peerId: Int = 0
        set(value) {
            if (field == 0) {
                field = value
            }
        }

    fun getUser() = userLiveData as WrappedLiveData<User>

    fun getProgress() = progressLiveData as WrappedLiveData<Pair<Int, Int>>

    fun analyse() {
        loadUser()
        loadDialogs()
    }

    private fun loadDialogs() {
        api.getMessagesLite(peerId, COUNT, progressLiveData.value?.data?.first ?: 0)
                .repeat(Long.MAX_VALUE)
                .takeUntil { response ->
                    response.response?.apply {
                        if ((progressLiveData.value?.data?.second ?: 0) == 0) {
                            updateTotalCount(count)
                        }
                        updateLoadedCount(items.size)
                    }
                    isFullyLoaded()
                }
                .subscribeSmart({}, {
                })
    }

    private fun loadUser() {
        api.getUsers("$peerId")
                .subscribeSmart({ users ->
                    userLiveData.value = Wrapper(users.getOrNull(0))
                }, { error ->
                    userLiveData.value = Wrapper(error = error)
                })
    }

    private fun isFullyLoaded(): Boolean {
        val loadedCount = progressLiveData.value?.data?.first ?: return false
        val totalCount = progressLiveData.value?.data?.second ?: return false
        return loadedCount >= totalCount
    }

    private fun updateTotalCount(totalCount: Int) {
        progressLiveData.value = Wrapper(Pair(0, totalCount))
    }

    private fun updateLoadedCount(deltaLoadedCount: Int) {
        val loadedCount = progressLiveData.value?.data?.first ?: return
        val totalCount = progressLiveData.value?.data?.second ?: return

        progressLiveData.value = Wrapper(Pair(loadedCount + deltaLoadedCount, totalCount))
    }

    companion object {

        const val COUNT = 200
    }

    class Factory @Inject constructor(private val api: ApiService) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass == AnalyseDialogViewModel::class.java) {
                return AnalyseDialogViewModel(api) as T
            }
            throw IllegalArgumentException("Unknown ViewModel $modelClass")
        }
    }
}