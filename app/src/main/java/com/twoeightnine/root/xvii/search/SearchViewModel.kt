package com.twoeightnine.root.xvii.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.twoeightnine.root.xvii.dialogs.models.Dialog
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.model.WrappedLiveData
import com.twoeightnine.root.xvii.model.WrappedMutableLiveData
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.mvp.presenter.UserResponse
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.utils.subscribeSmart
import io.reactivex.Flowable
import io.reactivex.functions.BiFunction
import javax.inject.Inject

class SearchViewModel(private val api: ApiService) : ViewModel() {

    private var page = 0

    private val resultLiveData = WrappedMutableLiveData<ArrayList<Dialog>>()

    fun getResult() = resultLiveData as WrappedLiveData<ArrayList<Dialog>>

    fun search(q: String) {
        if (q.isEmpty()) {
            resultLiveData.value = Wrapper(arrayListOf())
            return
        }
        Flowable.zip(
                api.searchFriends(q, User.FIELDS, COUNT, page * COUNT),
                api.searchUsers(q, User.FIELDS, COUNT, page * COUNT),
                ResponseCombinerFunction()
        )
                .subscribeSmart({ response ->
                    val existing = if (page == 0) {
                        arrayListOf()
                    } else {
                        resultLiveData.value?.data ?: arrayListOf()
                    }

                    val dialogs = response.items.map { user ->
                        Dialog(
                                peerId = user.id,
                                title = user.fullName,
                                photo = user.photo100,
                                isOnline = user.isOnline
                        )
                    }
                    resultLiveData.value = Wrapper(existing.apply { addAll(dialogs) })
                }, { error ->
                    resultLiveData.value = Wrapper(error = error)
                })
    }

    companion object {

        const val COUNT = 200
    }

    private inner class ResponseCombinerFunction :
            BiFunction<UserResponse, UserResponse, UserResponse> {

        override fun apply(t1: UserResponse, t2: UserResponse): UserResponse {
            t1.response?.items?.addAll(t2.response?.items ?: arrayListOf())
            return t1
        }
    }

    class Factory @Inject constructor(private val api: ApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass == SearchViewModel::class.java) {
                return SearchViewModel(api) as T
            }
            throw IllegalArgumentException("Unknown ViewModel $modelClass")
        }
    }
}