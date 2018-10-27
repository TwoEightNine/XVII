package com.twoeightnine.root.xvii.picturer

import com.twoeightnine.root.xvii.dagger.ApiService
import com.twoeightnine.root.xvii.model.Photo
import com.twoeightnine.root.xvii.mvp.BasePresenter
import com.twoeightnine.root.xvii.utils.subscribeSmart
import kotlin.math.roundToInt

class PicturerPresenter(api: ApiService) : BasePresenter<PicturerView>(api) {

    private val photos = arrayListOf<Photo>()
    private var count = 100L

    private val CNT = 6

    fun loadPictures(groupId: Int) {
        checkCount(groupId)
    }

    private fun loadPhotos(groupId: Int) {
        photos.clear()
        for (i in 0 until CNT) {
            api.getPhotos(-groupId, "wall", 1, rand())
                    .subscribeSmart({
                        photos.add(it.items[0])
                        if (photos.size == CNT) {
                            view?.hideLoading()
                            view?.onImagesLoaded(photos)
                        }
                    }, {
                        view?.hideLoading()
                        view?.showError(it)
                    })
        }
    }

    private fun checkCount(groupId: Int) {
        view?.showLoading()
        api.getPhotos(-groupId, "wall", 1, 0)
                .subscribeSmart({
                    count = it.count
                    loadPhotos(groupId)
                }, {
                    view?.hideLoading()
                    view?.showError(it)
                })
    }

    private fun rand() = (Math.random() * count).roundToInt()

}