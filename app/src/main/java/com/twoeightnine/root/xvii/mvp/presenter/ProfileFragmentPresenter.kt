package com.twoeightnine.root.xvii.mvp.presenter

import com.twoeightnine.root.xvii.dagger.ApiService
import com.twoeightnine.root.xvii.lg.Lg
import com.twoeightnine.root.xvii.model.Photo
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.mvp.BasePresenter
import com.twoeightnine.root.xvii.mvp.view.ProfileFragmentView
import com.twoeightnine.root.xvii.utils.subscribeSmart

class ProfileFragmentPresenter(api: ApiService,
                               var userId: Int): BasePresenter<ProfileFragmentView>(api) {

    val COUNT = 100
    var user: User? = null

    val photos = arrayListOf<Photo>()

    fun loadUser() {
        view?.showLoading()
        api.getUsers("$userId", User.FIELDS)
                .subscribeSmart({
                    response ->
                    user = response[0]
                    view?.hideLoading()
                    view?.onUserLoaded(user as User)
                    loadFoaf()
                }, {
                    error ->
                    view?.hideLoading()
                    view?.showError(error)
                })
    }

    fun loadFoaf() {
        api.getFoaf("https://vk.com/foaf.php", userId)
                .compose(applySchedulers())
                .subscribe({
                    response ->
                    view?.onFoafLoaded(getDate(response.string()))
                }, {
                    Lg.i("error in foaf: ${it.message}")
                })
    }

    fun getDate(site: String): String {
        val re = Regex("<ya:created dc:date=\"[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9:+-]*\"/>")
        val date = re.find(site)?.value?.substring(21, 31) ?: return ""
        return "${date.substring(8)}.${date.substring(5, 7)}.${date.substring(0, 4)}"
    }

    fun loadProfilePhotos() {
        if (photos.size != 0) {
            view?.onPhotosLoaded(photos)
            return
        }
        photos.clear()
        view?.showLoading()
        api.getPhotos(userId, "profile", COUNT, 0)
                .subscribeSmart({
                    response ->
                    photos.addAll(response.items)
                    view?.hideLoading()
                    view?.onPhotosLoaded(photos)
                }, {
                    error ->
                    view?.hideLoading()
                    view?.showError(error)
                })
    }

}