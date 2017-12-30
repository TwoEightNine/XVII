package com.twoeightnine.root.xvii.music

import com.twoeightnine.root.xvii.mvp.BaseView

interface MusicFragmentView: BaseView {

    fun onSearchStarted(id: Int, r: Int)
    fun onTracksLoaded(tracks: MutableList<Track>)
    fun onTrackIdLoaded(trackId: Int)

}