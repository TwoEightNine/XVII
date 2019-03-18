package com.twoeightnine.root.xvii.profile.viewmodels

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.twoeightnine.root.xvii.dagger.ApiService
import com.twoeightnine.root.xvii.managers.Lg
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.model.WrappedLiveData
import com.twoeightnine.root.xvii.model.WrappedMutableLiveData
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.utils.applySchedulers
import com.twoeightnine.root.xvii.utils.subscribeSmart
import javax.inject.Inject

class ProfileViewModel(private val api: ApiService) : ViewModel() {

    private val userLiveData = WrappedMutableLiveData<User>()
    private val foafLiveData = WrappedMutableLiveData<String>()

    fun getUser() = userLiveData as WrappedLiveData<User>

    fun getFoaf() = foafLiveData as WrappedLiveData<String>

    fun loadUser(userId: Int) {
        api.getUsers("$userId")
                .subscribeSmart({ users ->
                    val user = users[0]
                    userLiveData.value = Wrapper(user)
                    loadFoaf(userId)
                }, { error ->
                    userLiveData.value = Wrapper(error = error)
                })
    }

    @SuppressLint("CheckResult")
    fun loadFoaf(userId: Int) {
        api.getFoaf("https://vk.com/foaf.php", userId)
                .compose(applySchedulers())
                .subscribe({ response ->
                    foafLiveData.value = Wrapper(getFoafDate(response.string()))
                }, {
                    Lg.i("error in foaf: ${it.message}")
                    // no error in ui
                })
    }

    private fun getFoafDate(site: String): String {
        val re = Regex("<ya:created dc:date=\"[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9:+-]*\"/>")
        val date = re.find(site)?.value?.substring(21, 31) ?: return ""
        return "${date.substring(8)}.${date.substring(5, 7)}.${date.substring(0, 4)}"
    }

    class Factory @Inject constructor(private val api: ApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>) = ProfileViewModel(api) as T
    }
}