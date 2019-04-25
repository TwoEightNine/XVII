package com.twoeightnine.root.xvii.chats.messages.chat

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.messages.base.BaseMessagesFragment
import com.twoeightnine.root.xvii.chats.messages.base.MessagesAdapter
import com.twoeightnine.root.xvii.dialogs.fragments.DialogsForwardFragment
import com.twoeightnine.root.xvii.model.Message2
import com.twoeightnine.root.xvii.model.attachments.Doc
import com.twoeightnine.root.xvii.model.attachments.Photo
import com.twoeightnine.root.xvii.model.attachments.Video
import com.twoeightnine.root.xvii.photoviewer.ImageViewerActivity
import com.twoeightnine.root.xvii.profile.activities.ProfileActivity
import com.twoeightnine.root.xvii.utils.copyToClip
import com.twoeightnine.root.xvii.utils.getContextPopup
import com.twoeightnine.root.xvii.utils.showError
import com.twoeightnine.root.xvii.web.VideoViewerActivity

class ChatMessagesFragment : BaseMessagesFragment<ChatMessagesViewModel>() {

    private val peerId by lazy { arguments?.getInt(ARG_PEER_ID) ?: 0 }

    override fun getViewModelClass() = ChatMessagesViewModel::class.java

    override fun inject() {
        App.appComponent?.inject(this)
    }

    override fun prepareViewModel() {
        viewModel.peerId = peerId
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.clear()
        inflater?.inflate(R.menu.menu_chat, menu)
    }

    override fun getAdapterSettings() = MessagesAdapter.Settings(
            isImportant = false
    )

    override fun getAdapterCallback() = object : MessagesAdapter.Callback {
        override fun onClicked(message: Message2) {
            getContextPopup(context ?: return, R.layout.popup_message) {
                when (it.id) {
                    R.id.llCopy -> copyToClip(message.text)
//                    R.id.llEdit -> {}
//                    R.id.llReply -> presenter.attachUtils.forwarded = "${message.id}"
                    R.id.llForward -> {
                        rootActivity?.loadFragment(DialogsForwardFragment.newInstance("${message.id}"))
                    }
//                    R.id.llDelete -> {
//                        val callback = { forAll: Boolean ->
//                            presenter.deleteMessages(mutableListOf(message.id), forAll)
//                            CacheHelper.deleteMessagesAsync(mutableListOf(message.id))
//                        }
//                        if (message.isOut && time() - message.date < 3600 * 24) {
//                            showDeleteMessagesDialog(callback)
//                        } else {
//                            showDeleteDialog(safeActivity) { callback.invoke(false) }
//                        }
//                    }
//                    R.id.llDecrypt -> {
//                        message.body = getDecrypted(message.body)
//                        adapter.notifyItemChanged(adapter.items.indexOf(message))
//                    }
//                    R.id.llMarkImportant ->
//                        presenter.markAsImportant(
//                                mutableListOf(message.id),
//                                1
//                        )
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
        const val ARG_PEER_ID = "peerId"

        fun newInstance(peerId: Int): ChatMessagesFragment {
            val fragment = ChatMessagesFragment()
            fragment.arguments = Bundle().apply {
                putInt(ARG_PEER_ID, peerId)
            }
            return fragment
        }
    }
}