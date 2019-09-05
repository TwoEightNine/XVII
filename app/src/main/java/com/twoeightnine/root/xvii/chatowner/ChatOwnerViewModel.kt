package com.twoeightnine.root.xvii.chatowner

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.chatowner.model.ChatOwner
import com.twoeightnine.root.xvii.lg.Lg
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.model.*
import com.twoeightnine.root.xvii.model.attachments.Photo
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.utils.subscribeSmart
import javax.inject.Inject

class ChatOwnerViewModel : ViewModel() {

    @Inject
    lateinit var api: ApiService

    private val chatOwnerLiveData = WrappedMutableLiveData<ChatOwner>()
    private val photosLiveData = MutableLiveData<List<Photo>>()

    val chatOwner: WrappedLiveData<ChatOwner>
        get() = chatOwnerLiveData

    val photos: LiveData<List<Photo>>
        get() = photosLiveData

    init {
        App.appComponent?.inject(this)
    }

    fun <T : ChatOwner> loadChatOwner(peerId: Int, chatOwnerClass: Class<T>) {
        when (chatOwnerClass) {
            User::class.java -> loadUser(peerId)
            Group::class.java -> loadGroup(-peerId)
            Conversation::class.java -> loadConversation(peerId)
        }
    }

    fun <T : ChatOwner> loadPhotos(peerId: Int, chatOwnerClass: Class<T>) {
        api.getPhotos(peerId, PHOTOS_ALBUM, PHOTOS_COUNT)
                .subscribeSmart({
                    photosLiveData.value = it.items
                }, { error ->
                    Lg.wtf("cant load photos for $peerId: $error")
                })
    }

    fun getShowNotifications(peerId: Int) = peerId !in Prefs.muteList

    fun setShowNotifications(peerId: Int, show: Boolean) {
        val muteList = Prefs.muteList
        val inMuteList = peerId in muteList
        when {
            show && inMuteList -> {
                muteList.remove(peerId)
            }
            !show && !inMuteList -> {
                muteList.add(peerId)
            }
        }
        Prefs.muteList = muteList
    }

    private fun loadUser(userId: Int) {
        api.getUsers(userId.toString())
                .subscribeSmart({
                    chatOwnerLiveData.value = Wrapper(it.getOrNull(0))
                }, { error ->
                    chatOwnerLiveData.value = Wrapper(error = error)
                })
    }

    private fun loadGroup(id: Int) {
        api.getGroups(id.toString())
                .subscribeSmart({
                    chatOwnerLiveData.value = Wrapper(it.getOrNull(0))
                }, { error ->
                    chatOwnerLiveData.value = Wrapper(error = error)
                })
    }

    private fun loadConversation(peerId: Int) {
        api.getConversationsById(peerId.toString())
                .subscribeSmart({
                    chatOwnerLiveData.value = Wrapper(it.items.getOrNull(0))
                }, { error ->
                    chatOwnerLiveData.value = Wrapper(error = error)
                })
    }

    companion object {
        const val PHOTOS_ALBUM = "profile"
        const val PHOTOS_COUNT = 100
    }

}