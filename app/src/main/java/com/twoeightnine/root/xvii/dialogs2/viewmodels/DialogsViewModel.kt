package com.twoeightnine.root.xvii.dialogs2.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.twoeightnine.root.xvii.App.Companion.context
import com.twoeightnine.root.xvii.db.AppDb
import com.twoeightnine.root.xvii.dialogs2.models.Dialog
import com.twoeightnine.root.xvii.model.WrappedLiveData
import com.twoeightnine.root.xvii.model.WrappedMutableLiveData
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.network.response.ConversationsResponse
import com.twoeightnine.root.xvii.network.response.ServerResponse
import com.twoeightnine.root.xvii.utils.subscribeSmart
import javax.inject.Inject

class DialogsViewModel(
        private val api: ApiService,
        private val appDb: AppDb
) : ViewModel() {

    private val dialogsLiveData = WrappedMutableLiveData<ArrayList<Dialog>>()

    fun getDialogs() = dialogsLiveData as WrappedLiveData<ArrayList<Dialog>>

    fun loadDialogs(offset: Int = 0) {
        api.getConversations(COUNT, offset)
                .map { process(it) }
                .subscribeSmart({ dialogs ->
                    dialogsLiveData.value = Wrapper(dialogs)
                    saveDialogs(dialogs)
                }, { error ->
                    dialogsLiveData.value = Wrapper(error = error)
                })
    }

    private fun process(resp: ServerResponse<ConversationsResponse>): ServerResponse<ArrayList<Dialog>> {
        val dialogs = arrayListOf<Dialog>()
        val response = resp.response
        response?.items?.forEach { dm ->
            dialogs.add(Dialog(
                    dm.lastMessage.peerId,
                    response.getTitleFor(dm) ?: "???",
                    response.getPhotoFor(dm),
                    dm.lastMessage.getResolvedMessage(context) ?: "",
                    dm.lastMessage.date,
                    dm.lastMessage.out == 1,
                    dm.conversation.isRead(),
                    dm.conversation.unreadCount
            ))
        }
        return ServerResponse(dialogs, resp.error)
    }

    private fun saveDialogs(dialogs: ArrayList<Dialog>) {
        appDb.dialogsDao().insertDialogs(*dialogs.toTypedArray())
    }

    companion object {
        const val COUNT = 100
    }

    class Factory @Inject constructor(
            private val api: ApiService,
            private val appDb: AppDb
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DialogsViewModel::class.java)) {
                return DialogsViewModel(api, appDb) as T
            }
            throw IllegalArgumentException("Unknown $modelClass")
        }
    }
}