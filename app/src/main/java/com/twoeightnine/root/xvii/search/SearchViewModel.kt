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

package com.twoeightnine.root.xvii.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.model.WrappedLiveData
import com.twoeightnine.root.xvii.model.WrappedMutableLiveData
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.network.response.BaseResponse
import com.twoeightnine.root.xvii.network.response.ListResponse
import com.twoeightnine.root.xvii.network.response.SearchConversationsResponse
import com.twoeightnine.root.xvii.network.response.SearchResponse
import com.twoeightnine.root.xvii.utils.subscribeSmart
import global.msnthrp.xvii.data.dialogs.Dialog
import io.reactivex.Flowable
import io.reactivex.functions.Function3
import java.lang.StrictMath.min
import javax.inject.Inject

class SearchViewModel(private val api: ApiService) : ViewModel() {

    var fromFriendsPage = false

    private val resultLiveData = WrappedMutableLiveData<ArrayList<SearchDialog>>()

    fun getResult() = resultLiveData as WrappedLiveData<ArrayList<SearchDialog>>

    fun setFrom(fromFriends:Boolean){
        fromFriendsPage = fromFriends
    }

    fun search(q: String, offset: Int =0) {
        if (q.isEmpty()) {
            if (Prefs.suggestPeople) {
                api.searchUsers(q, User.FIELDS, COUNT, 0)
                        .subscribeSmart({ response ->
                            resultLiveData.value = Wrapper(ArrayList(response.items.map { createFromUser(it) }))
                        }, { error ->
                            resultLiveData.value = Wrapper(error = error)
                        })
            } else {
                resultLiveData.value = Wrapper(arrayListOf())
            }
        } else {
            if (!fromFriendsPage) {
                api.search(q, COUNT, offset).subscribeSmart({ response ->
                    val dialogs = arrayListOf<SearchDialog>()
                    val mResp = response
                    mResp?.items?.forEach { msg ->
                        var dlg = SearchDialog(
                            peerId = msg.peerId ?: 0,
                            messageId = msg.id ?: 0,
                            text = msg.text ?: "",
                            title = mResp.getTitleFor(msg) ?: "",
                            photo = mResp.getPhotoFor(msg) ?: "",
                            isOnline = mResp.isOnline(msg),
                            isOut = msg.isOut(),
                            isChat = true
                        )
                        dialogs.add(dlg)
                    }
                    if(offset > 0) {
                        resultLiveData.value?.data?.addAll(ArrayList(dialogs.distinctBy { it.messageId }))
                        resultLiveData.value = Wrapper(resultLiveData.value?.data)
                    }else {
                        resultLiveData.value =
                            Wrapper(ArrayList(dialogs.distinctBy { it.messageId }))
                    }
                }, { error ->
                    resultLiveData.value = Wrapper(error = error)
                })
            }else{
                Flowable.zip(
                    api.searchFriends(q, User.FIELDS, COUNT, offset),
                    api.searchUsers(q, User.FIELDS, COUNT, offset),
                    api.searchConversations(q, COUNT),
                    ResponseCombinerFunction()
                )
                    .subscribeSmart({ response ->
                        if(offset > 0) {
                            resultLiveData.value?.data?.addAll(ArrayList(response.distinctBy { it.peerId }))
                            resultLiveData.value = Wrapper(resultLiveData.value?.data)
                        }else {
                            resultLiveData.value = Wrapper(ArrayList(response.distinctBy { it.peerId }))
                        }

                    }, { error ->
                        resultLiveData.value = Wrapper(error = error)
                    })

            }
        }
    }

    private fun createFromUser(user: User) = SearchDialog(
            peerId = user.id,
            messageId = user.id,
            title = user.fullName,
            photo = user.photo100,
            isOnline = user.isOnline
    )

    companion object {

        const val COUNT = 50
    }

    private inner class ResponseCombinerFunction :
            Function3<BaseResponse<ListResponse<User>>,
                    BaseResponse<ListResponse<User>>,
                    BaseResponse<SearchConversationsResponse>,
                    BaseResponse<ArrayList<SearchDialog>>> {

        override fun apply(
                friends: BaseResponse<ListResponse<User>>,
                users: BaseResponse<ListResponse<User>>,
                conversations: BaseResponse<SearchConversationsResponse>
        ): BaseResponse<ArrayList<SearchDialog>> {
            val dialogs = arrayListOf<SearchDialog>()

            // @TODO: может, убрать  conversation? В диалогах и так работает
            // поиск по имени беседы, а на вкладке пользователей это не вполне надо
             val cResp = conversations.response
            friends.response?.items?.forEach { dialogs.add(createFromUser(it)) }
            cResp?.items?.forEach { conversation ->
                dialogs.add(SearchDialog(
                        peerId = conversation.peer?.id ?: 0,
                        messageId = conversation.peer?.id ?: 0,
                        title = cResp.getTitleFor(conversation) ?: "",
                        photo = cResp.getPhotoFor(conversation) ?: "",
                        isOnline = cResp.isOnline(conversation)
                ))
            }
            users.response?.items?.forEach { dialogs.add(createFromUser(it)) }
            return BaseResponse(dialogs)
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