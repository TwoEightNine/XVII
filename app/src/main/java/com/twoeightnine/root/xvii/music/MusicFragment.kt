package com.twoeightnine.root.xvii.music

import android.os.Bundle
import android.view.View
import android.widget.ListView
import butterknife.BindView
import butterknife.ButterKnife
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.activities.RootActivity
import com.twoeightnine.root.xvii.background.MediaPlayerAsyncTask
import com.twoeightnine.root.xvii.dagger.MusicService
import com.twoeightnine.root.xvii.fragments.BaseFragment
import com.twoeightnine.root.xvii.utils.showError
import com.twoeightnine.root.xvii.views.LoaderView
import javax.inject.Inject

class MusicFragment: BaseFragment(), MusicFragmentView {

    @BindView(R.id.lvTracks)
    lateinit var lvTracks: ListView
    @BindView(R.id.loader)
    lateinit var loader: LoaderView

    @Inject
    lateinit var api: MusicService
    lateinit var presenter: MusicFragmentPresenter

    private lateinit var adapter: TracksAdapter

    override fun getLayout() = R.layout.fragment_music

    override fun bindViews(view: View) {
        ButterKnife.bind(this, view)
        initAdapter()
        App.appComponent?.inject(this)
        presenter = MusicFragmentPresenter(api, this)
        presenter.startSearch("galat")
    }

    fun initAdapter() {
        adapter = TracksAdapter()
        lvTracks.adapter = adapter
        lvTracks.setOnItemClickListener {
            _, _, pos, _ ->
            val track = adapter.items[pos]
            presenter.selectTrack(track.preUrl)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        updateTitle("Music", "")
    }

    fun getUrl(trackId: Int) = "http://sound.ka4ka.ru/index.php?mod=dnl&act=get&id=$trackId"

    override fun showLoading() {
        loader.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        loader.visibility = View.GONE
    }

    override fun showError(error: String) {
        showError(rootActivity, error)
    }

    override fun onSearchStarted(id: Int, r: Int) {
        presenter.finishSearch(id, r)
    }

    override fun onTracksLoaded(tracks: MutableList<Track>) {
        adapter.add(tracks)
    }

    override fun onTrackIdLoaded(trackId: Int) {
        RootActivity.player = MediaPlayerAsyncTask {
            RootActivity.player = null
        }
        RootActivity.player!!.execute(getUrl(trackId))
    }
}