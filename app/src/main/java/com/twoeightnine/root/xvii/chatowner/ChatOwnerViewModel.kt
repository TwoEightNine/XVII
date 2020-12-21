package com.twoeightnine.root.xvii.chatowner

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.chatowner.model.ChatOwner
import com.twoeightnine.root.xvii.db.AppDb
import com.twoeightnine.root.xvii.lg.L
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.managers.Session
import com.twoeightnine.root.xvii.model.*
import com.twoeightnine.root.xvii.model.attachments.Photo
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.utils.*
import java.util.*
import javax.inject.Inject

class ChatOwnerViewModel : ViewModel() {

    @Inject
    lateinit var api: ApiService

    @Inject
    lateinit var appDb: AppDb

    private val chatOwnerLiveData = WrappedMutableLiveData<ChatOwner>()
    private val photosLiveData = MutableLiveData<List<Photo>>()
    private val conversationMembersLiveData = MutableLiveData<List<User>>()
    private val titleLiveData = WrappedMutableLiveData<String>()
    private val blockedLiveData = WrappedMutableLiveData<Boolean>()
    private val foafLiveData = WrappedMutableLiveData<Date>()
    private val aliasLiveData = MutableLiveData<String>()

    val chatOwner: WrappedLiveData<ChatOwner>
        get() = chatOwnerLiveData

    val photos: LiveData<List<Photo>>
        get() = photosLiveData

    val conversationMembers: LiveData<List<User>>
        get() = conversationMembersLiveData

    val title: WrappedLiveData<String>
        get() = titleLiveData

    val blocked: WrappedLiveData<Boolean>
        get() = blockedLiveData

    val foaf: WrappedLiveData<Date>
        get() = foafLiveData

    val alias: LiveData<String>
        get() = aliasLiveData

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
                    L.tag(TAG).warn().log("cant load photos for $peerId: $error")
                })
    }

    fun loadChatMembers(peerId: Int) {
        api.getConversationMembers(peerId)
                .subscribeSmart({
                    conversationMembersLiveData.value = it.profiles
                }, { error ->
                    L.tag(TAG).warn().log("cant load members for $peerId: $error")
                })
    }

    fun changeChatTitle(peerId: Int, newTitle: String) {
        api.editChatTitle(peerId.asChatId(), newTitle)
                .subscribeSmart({
                    if (it == 1) {
                        titleLiveData.value = Wrapper(newTitle)
                    }
                }, { error ->
                    titleLiveData.value = Wrapper(error = error)
                })
    }

    fun kickUser(peerId: Int, userId: Int) {
        api.kickUser(peerId.asChatId(), userId)
                .subscribeSmart({
                    if (it == 1) {
                        val members = conversationMembers.value ?: arrayListOf()
                        conversationMembersLiveData.value = members.filter { it.id != userId }
                    }
                }, {})
    }

    fun leaveConversation(peerId: Int) {
        kickUser(peerId, Session.uid)
    }

    fun blockUser(userId: Int) {
        api.blockUser(userId)
                .subscribeSmart({
                    blockedLiveData.value = Wrapper(it == 1)
                }, { error ->
                    blockedLiveData.value = Wrapper(error = error)
                })
    }

    fun unblockUser(userId: Int) {
        api.unblockUser(userId)
                .subscribeSmart({
                    blockedLiveData.value = Wrapper(it != 1)
                }, { error ->
                    blockedLiveData.value = Wrapper(error = error)
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

    @SuppressLint("CheckResult")
    fun loadFoaf(peerId: Int) {
        if (!peerId.matchesUserId()) return
        api.getFoaf("https://vk.com/foaf.php", peerId)
                .compose(applySchedulers())
                .subscribe({ response ->
                    foafLiveData.value = Wrapper(getFoafDate(response.string()))
                }, {
                    // no error in ui
                })
    }

    @SuppressLint("CheckResult")
    fun loadAlias(peerId: Int) {
        appDb.dialogsDao()
                .getDialogs(peerId)
                .compose(applySingleSchedulers())
                .map { it.alias ?: "" }
                .subscribe { alias ->
                    if (alias.isNotBlank()) {
                        aliasLiveData.value = alias
                    }
                }
    }

    private fun getFoafDate(site: String): Date? {
        val re = Regex("<ya:created dc:date=\"[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9:+-]*\"/>")
        val dateString = re.find(site)?.value?.substring(21, 31) ?: return null
        val day = dateString.substring(8).toInt()
        val month = dateString.substring(5, 7).toInt()
        val year = dateString.substring(0, 4).toInt()
        val date = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
        }
        return date.time
    }

    private fun loadUser(userId: Int) {
        api.getUsers(userId.toString())
                .subscribeSmart({
                    val user = it.getOrNull(0)
                    blockedLiveData.value = Wrapper(user?.blacklistedByMe == 1)
                    chatOwnerLiveData.value = Wrapper(user)
                }, { error ->
                    blockedLiveData.value = Wrapper(false)
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
        private const val TAG = "chat owner"
        const val PHOTOS_ALBUM = "profile"
        const val PHOTOS_COUNT = 100
    }

}