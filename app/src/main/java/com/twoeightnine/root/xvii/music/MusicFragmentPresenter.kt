package com.twoeightnine.root.xvii.music

import com.twoeightnine.root.xvii.dagger.MusicService
import com.twoeightnine.root.xvii.utils.applySchedulers
import java.util.concurrent.TimeUnit

class MusicFragmentPresenter(var api: MusicService, var view: MusicFragmentView) {

    fun startSearch(query: String) {
        view.showLoading()
//        api.startSearch(query)
//                .delay(3L, TimeUnit.SECONDS)
//                .compose(applySchedulers())
//                .subscribe({
//                    response ->
//                    val data = getSearchData(response.string())
//                    view.hideLoading()
//                    view.onSearchStarted(data[0], data[1])
//                }, {
//                    view.hideLoading()
//                    view.showError(it.message ?: "null")
//                })
    }

    fun finishSearch(id: Int, r: Int) {
        view.showLoading()
//        api.finishSearch(id, r)
//                .compose(applySchedulers())
//                .subscribe({
//                    response ->
//                    val tracks = getTracks(response.string())
//                    view.hideLoading()
//                    view.onTracksLoaded(tracks)
//
//                }, {
//                    view.hideLoading()
//                    view.showError(it.message ?: "null")
//                })
    }

    fun selectTrack(trackUrl: TrackUrl) {
        view.showLoading()
//        api.selectTrack(trackUrl.one, trackUrl.two, trackUrl.three, trackUrl.idCode, trackUrl.s)
//                .delay(5L, TimeUnit.SECONDS)
//                .compose(applySchedulers())
//                .subscribe({
//                    response ->
//                    val trackId = getTrackId(response.string())
//                    view.hideLoading()
//                    view.onTrackIdLoaded(trackId)
//                }, {
//                    view.hideLoading()
//                    view.showError(it.message ?: "null")
//                })
    }

}