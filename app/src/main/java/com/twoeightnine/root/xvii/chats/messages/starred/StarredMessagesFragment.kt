package com.twoeightnine.root.xvii.chats.messages.starred

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.RelativeLayout
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chatowner.ChatOwnerActivity
import com.twoeightnine.root.xvii.chats.messages.base.BaseMessagesFragment
import com.twoeightnine.root.xvii.chats.messages.base.MessagesAdapter
import com.twoeightnine.root.xvii.dialogs.activities.DialogsForwardActivity
import com.twoeightnine.root.xvii.model.attachments.Doc
import com.twoeightnine.root.xvii.model.attachments.Photo
import com.twoeightnine.root.xvii.model.attachments.Video
import com.twoeightnine.root.xvii.model.messages.Message
import com.twoeightnine.root.xvii.photoviewer.ImageViewerActivity
import com.twoeightnine.root.xvii.utils.*
import com.twoeightnine.root.xvii.utils.contextpopup.ContextPopupItem
import com.twoeightnine.root.xvii.utils.contextpopup.createContextPopup
import com.twoeightnine.root.xvii.web.VideoViewerActivity
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.view_chat_multiselect.*

class StarredMessagesFragment : BaseMessagesFragment<StarredMessagesViewModel>() {

    override fun getViewModelClass() = StarredMessagesViewModel::class.java

    override fun inject() {
        App.appComponent?.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (rlInput.layoutParams as? RelativeLayout.LayoutParams)?.height = 0
        ivReplyMulti.visibility = View.INVISIBLE
        ivDeleteMulti.visibility = View.INVISIBLE
        ivMarkMulti.visibility = View.INVISIBLE
        rlMultiAction.stylizeAll()
        rlMultiAction.stylizeColor()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        xviiToolbar.title = getString(R.string.important)
        rvChatList.setBottomInsetPadding()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }


    override fun getAdapterSettings() = MessagesAdapter.Settings(
            isImportant = true
    )

    override fun getAdapterCallback() = object : MessagesAdapter.Callback {

        override fun onClicked(message: Message) {
            createContextPopup(context ?: return, arrayListOf(
                    ContextPopupItem(R.drawable.ic_copy_popup, R.string.copy) {
                        copyToClip(message.text)
                    },
                    ContextPopupItem(R.drawable.ic_star_crossed, R.string.unmark) {
                        viewModel.unmarkMessage(message)
                    },
                    ContextPopupItem(R.drawable.ic_transfer_popup, R.string.forward) {
                        DialogsForwardActivity.launch(context, message.id.toString())
                    }
            )).show()
        }

        override fun onUserClicked(userId: Int) {
            ChatOwnerActivity.launch(context, userId)
        }

        override fun onEncryptedFileClicked(doc: Doc) {
        }

        override fun onPhotoClicked(position: Int, photos: ArrayList<Photo>) {
            ImageViewerActivity.viewImages(context, photos, position)
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