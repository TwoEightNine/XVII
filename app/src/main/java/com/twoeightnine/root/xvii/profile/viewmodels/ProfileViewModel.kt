package com.twoeightnine.root.xvii.profile.viewmodels

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.twoeightnine.root.xvii.background.longpoll.models.events.OfflineEvent
import com.twoeightnine.root.xvii.background.longpoll.models.events.OnlineEvent
import com.twoeightnine.root.xvii.lg.Lg
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.model.WrappedLiveData
import com.twoeightnine.root.xvii.model.WrappedMutableLiveData
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.model.attachments.Photo
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.utils.EventBus
import com.twoeightnine.root.xvii.utils.applySchedulers
import com.twoeightnine.root.xvii.utils.subscribeSmart
import javax.inject.Inject

class ProfileViewModel(private val api: ApiService) : ViewModel() {

    init {
        EventBus.subscribeLongPollEventReceived { event ->
            when (event) {
                is OnlineEvent -> if (event.userId == userId) {
                    updateStatus(true, event.timeStamp, event.deviceCode)
                }
                is OfflineEvent -> if (event.userId == userId) {
                    updateStatus(false, event.timeStamp)
                }
            }
        }
    }

    var userId: Int = 0
        set(value) {
            if (userId == 0) {
                field = value
            }
        }

    private val photos = arrayListOf<Photo>()

    private val userLiveData = WrappedMutableLiveData<User>()
    private val foafLiveData = WrappedMutableLiveData<String>()

    fun getUser() = userLiveData as WrappedLiveData<User>

    fun getFoaf() = foafLiveData as WrappedLiveData<String>

    fun loadUser() {
        api.getUsers("$userId")
                .subscribeSmart({ users ->
                    val user = users[0]
//                    CacheHelper.saveUserAsync(user)
                    userLiveData.value = Wrapper(user)
                    loadFoaf()
                }, { error ->
                    userLiveData.value = Wrapper(error = error)
                })
    }

    @SuppressLint("CheckResult")
    fun loadFoaf() {
        api.getFoaf("https://vk.com/foaf.php", userId)
                .compose(applySchedulers())
                .subscribe({ response ->
                    foafLiveData.value = Wrapper(getFoafDate(response.string()))
                }, {
                    Lg.i("error in foaf: ${it.message}")
                    // no error in ui
                })
    }

    fun loadPhotos(onLoaded: (ArrayList<Photo>) -> Unit) {
        if (photos.isNotEmpty()) {
            onLoaded(photos)
            return
        }

        api.getPhotos(userId, PHOTOS_ALBUM_ID, PHOTOS_COUNT)
                .subscribeSmart({ response ->
                    photos.addAll(response.items)
                    onLoaded(photos)
                }, { error ->
                    Lg.i("error loading photos: $error")
                })
    }

    private fun getFoafDate(site: String): String {
        val re = Regex("<ya:created dc:date=\"[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9:+-]*\"/>")
        val date = re.find(site)?.value?.substring(21, 31) ?: return ""
        return "${date.substring(8)}.${date.substring(5, 7)}.${date.substring(0, 4)}"
    }

    private fun updateStatus(isOnline: Boolean, timeStamp: Int, platform: Int = 0) {
        val user = userLiveData.value?.data ?: return
        user.isOnline = isOnline
        user.lastSeen?.time = timeStamp
        if (platform != 0) {
            user.lastSeen?.platform = platform
        }
        userLiveData.value = Wrapper(user)
    }

    companion object {
        const val PHOTOS_COUNT = 100
        const val PHOTOS_ALBUM_ID = "profile"
    }

    class Factory @Inject constructor(private val api: ApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>) = ProfileViewModel(api) as T
    }
}