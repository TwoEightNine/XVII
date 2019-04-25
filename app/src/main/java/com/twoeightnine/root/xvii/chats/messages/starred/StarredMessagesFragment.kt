package com.twoeightnine.root.xvii.chats.messages.starred

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.messages.base.BaseMessagesFragment
import com.twoeightnine.root.xvii.chats.messages.base.MessagesAdapter
import com.twoeightnine.root.xvii.dialogs.fragments.DialogsForwardFragment
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.model.Message2
import com.twoeightnine.root.xvii.model.attachments.Doc
import com.twoeightnine.root.xvii.model.attachments.Photo
import com.twoeightnine.root.xvii.model.attachments.Video
import com.twoeightnine.root.xvii.photoviewer.ImageViewerActivity
import com.twoeightnine.root.xvii.profile.activities.ProfileActivity
import com.twoeightnine.root.xvii.utils.copyToClip
import com.twoeightnine.root.xvii.utils.getContextPopup
import com.twoeightnine.root.xvii.utils.hide
import com.twoeightnine.root.xvii.utils.showError
import com.twoeightnine.root.xvii.web.VideoViewerActivity
import kotlinx.android.synthetic.main.fragment_chat_new.*
import kotlinx.android.synthetic.main.toolbar.*

class StarredMessagesFragment : BaseMessagesFragment<StarredMessagesViewModel>() {

    override fun getViewModelClass() = StarredMessagesViewModel::class.java

    override fun inject() {
        App.appComponent?.inject(this)
    }

    override fun getHomeAsUpIcon() = R.drawable.ic_back

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        llInput.hide()
        ivReplyMulti.visibility = View.INVISIBLE
        ivMenuMulti.visibility = View.INVISIBLE
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        updateTitle(getString(R.string.important))
        Style.forToolbar(toolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.clear()
    }


    override fun getAdapterSettings() = MessagesAdapter.Settings(
            isImportant = true
    )

    override fun getAdapterCallback() = object : MessagesAdapter.Callback {

        override fun onClicked(message: Message2) {

            getContextPopup(context ?: return, R.layout.popup_important) {
                when (it.id) {
                    R.id.llCopy -> copyToClip(message.text)
                    R.id.llUnmark -> viewModel.unmarkMessage(message)
                    R.id.llForward -> rootActivity?.loadFragment(DialogsForwardFragment.newInstance("${message.id}"))
                }
            }.show()
        }

        override fun onUserClicked(userId: Int) {
//            rootActivity?.loadFragment(ProfileFragment.newInstance(userId))
            ProfileActivity.launch(context, userId)
        }

        override fun onEncryptedFileClicked(doc: Doc) {
        }

        override fun onPhotoClicked(photo: Photo) {
            ImageViewerActivity.viewImages(context, arrayListOf(photo))
        }

        override fun onVideoClicked(video: Video) {
            viewModel.loadVideo(context ?: return, video, { playerUrl ->
                VideoViewerActivity.launch(context, playerUrl)
            }, { error ->
                showError(context, error)
            })
        }
    }

    companion object {
        fun newInstance() = StarredMessagesFragment()
    }
}