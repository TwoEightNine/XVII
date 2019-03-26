package com.twoeightnine.root.xvii.chats.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.adapters.ChatAdapter
import com.twoeightnine.root.xvii.dialogs.fragments.DialogsForwardFragment
import com.twoeightnine.root.xvii.fragments.BaseOldFragment
import com.twoeightnine.root.xvii.model.Doc
import com.twoeightnine.root.xvii.model.Message
import com.twoeightnine.root.xvii.model.Photo
import com.twoeightnine.root.xvii.model.Video
import com.twoeightnine.root.xvii.mvp.presenter.ImportantFragmentPresenter
import com.twoeightnine.root.xvii.mvp.view.ImportantFragmentView
import com.twoeightnine.root.xvii.profile.fragments.ProfileFragment
import com.twoeightnine.root.xvii.utils.*
import kotlinx.android.synthetic.main.fragment_important.*
import javax.inject.Inject

class ImportantFragment : BaseOldFragment(), ImportantFragmentView {

    @Inject
    lateinit var presenter: ImportantFragmentPresenter
    @Inject
    lateinit var apiUtils: ApiUtils

    lateinit var adapter: ChatAdapter

    override fun bindViews(view: View) {
        super.bindViews(view)
        App.appComponent?.inject(this)
        presenter.view = this
        initAdapter()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        updateTitle(getString(R.string.important))
    }

    fun initAdapter() {
        rvImportant.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
        adapter = ChatAdapter(
                safeActivity,
                { presenter.loadHistory(it) },
                AdapterCallback(),
                true
        )
        val llm = LinearLayoutManager(activity)
        llm.stackFromEnd = true
        rvImportant.layoutManager = llm
        rvImportant.adapter = adapter
    }

    override fun onNew(view: View) {
        presenter.loadHistory()
    }

    override fun onRecovered(view: View) {
        adapter.stopLoading(presenter.getSaved())
    }

    override fun getLayout() = R.layout.fragment_important

    override fun showLoading() {
        adapter.startLoading()
    }

    override fun hideLoading() {
//        adapter.stopLoading()
    }

    override fun showError(error: String) {
        adapter.setErrorLoading()
        showError(activity, error)
    }

    override fun onHistoryLoaded(history: MutableList<Message>) {
        adapter.stopLoading(history, true)
    }

    override fun onHistoryClear() {
        adapter.clear()
    }

    override fun onMessagesDeleted(mids: MutableList<Int>) {
        for (mid in mids) {
            for (pos in adapter.items.indices) {
                if (adapter.items[pos].id == mid) {
                    adapter.removeAt(pos)
                    break
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.clear()
    }

    private inner class AdapterCallback : ChatAdapter.ChatAdapterCallback {

        override fun onClicked(message: Message) {}

        override fun onLongClicked(message: Message): Boolean {
            getContextPopup(safeActivity, R.layout.popup_important) {
                when (it.id) {
                    R.id.llCopy -> copyToClip(message.body ?: "")
                    R.id.llDelete -> showDeleteDialog(safeActivity) { presenter.deleteMessages(mutableListOf(message.id)) }
                    R.id.llForward -> rootActivity.loadFragment(DialogsForwardFragment.newInstance("${message.id}"))
                }
            }.show()
            return true
        }

        override fun onUserClicked(userId: Int) {
            rootActivity.loadFragment(ProfileFragment.newInstance(userId))
        }

        override fun onDocClicked(doc: Doc) {}

        override fun onPhotoClicked(photo: Photo) {
            apiUtils.showPhoto(safeActivity, photo.photoId, photo.accessKey)
        }

        override fun onVideoClicked(video: Video) {
            apiUtils.openVideo(safeActivity, video)
        }
    }
}