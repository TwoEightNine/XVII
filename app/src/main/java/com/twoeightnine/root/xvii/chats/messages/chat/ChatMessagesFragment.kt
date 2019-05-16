package com.twoeightnine.root.xvii.chats.messages.chat

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.ChatFragment
import com.twoeightnine.root.xvii.chats.attachments.attach.AttachActivity
import com.twoeightnine.root.xvii.chats.attachments.attach.AttachFragment
import com.twoeightnine.root.xvii.chats.attachments.attached.AttachedAdapter
import com.twoeightnine.root.xvii.chats.messages.base.BaseMessagesFragment
import com.twoeightnine.root.xvii.chats.messages.base.MessagesAdapter
import com.twoeightnine.root.xvii.chats.tools.ChatInputController
import com.twoeightnine.root.xvii.chats.tools.ChatToolbarController
import com.twoeightnine.root.xvii.dialogs.activities.DialogsForwardActivity
import com.twoeightnine.root.xvii.dialogs.models.Dialog
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.model.Message2
import com.twoeightnine.root.xvii.model.attachments.*
import com.twoeightnine.root.xvii.photoviewer.ImageViewerActivity
import com.twoeightnine.root.xvii.profile.activities.ProfileActivity
import com.twoeightnine.root.xvii.utils.*
import com.twoeightnine.root.xvii.views.LoadingDialog
import com.twoeightnine.root.xvii.views.TextInputAlertDialog
import com.twoeightnine.root.xvii.web.VideoViewerActivity
import kotlinx.android.synthetic.main.chat_input_panel.*
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.toolbar_chat.*

class ChatMessagesFragment : BaseMessagesFragment<ChatMessagesViewModel>() {

    private val peerId by lazy { arguments?.getInt(ARG_PEER_ID) ?: 0 }
    private val title by lazy { arguments?.getString(ARG_TITLE) ?: "" }
    private val photo by lazy { arguments?.getString(ARG_PHOTO) ?: "" }
    private val forwardedMessages by lazy { arguments?.getString(ARG_FORWARDED) }
    private val shareText by lazy { arguments?.getString(ARG_SHARE_TEXT) }
    private val shareImage by lazy { arguments?.getString(ARG_SHARE_IMAGE) }

    private val permissionHelper by lazy { PermissionHelper(this) }
    private val attachedAdapter by lazy {
        AttachedAdapter(contextOrThrow, ::onAttachClicked, inputController::setAttachedCount)
    }
    private val chatToolbarController by lazy {
        ChatToolbarController(toolbar)
    }

    private val handler = Handler()
    private var dialogLoading: LoadingDialog? = null
    private lateinit var inputController: ChatInputController

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
        inputController = ChatInputController(contextOrThrow, view, InputCallback())
        swipeContainer.setOnRefreshListener { /*presenter.loadHistory(withClear = true)*/ }

        rvAttached.layoutManager = LinearLayoutManager(context, LinearLayout.HORIZONTAL, false)
        rvAttached.adapter = attachedAdapter
        stylize()
        initContent()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        chatToolbarController.setTitle(title)
        if (peerId != -App.GROUP) {
            chatToolbarController.setAvatar(photo)
        }
        if (!peerId.matchesUserId()) {
            onOnlineChanged(false)
        }
        toolbar?.setOnClickListener {
            activity?.let { hideKeyboard(it) }
            if (peerId.matchesUserId()) {
                ProfileActivity.launch(context, peerId)
            }
        }
    }

    private fun stylize() {
        rlMultiAction.stylizeAll()
        rlMultiAction.stylizeColor()
        fabHasMore.stylize()
        rlBack.stylizeAll()

        if (Prefs.chatBack.isNotEmpty()) {
            try {
                flContainer.backgroundImage = Drawable.createFromPath(Prefs.chatBack)
            } catch (e: Exception) {
                Prefs.chatBack = ""
                showError(activity, e.message ?: "background not found")
            }
        }
    }

    private fun initContent() {
        forwardedMessages?.also {
            handler.postDelayed({
                attachedAdapter.fwdMessages = it
            }, 500L)
        }
        shareText?.also {
            handler.postDelayed({
                etInput.setText(it)
            }, 500L)
        }
        shareImage?.also {
            handler.postDelayed({
                onImagesSelected(arrayListOf(it))
            }, 500L)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.clear()
        inflater?.inflate(R.menu.menu_chat, menu)
    }

    private fun showDeleteMessagesDialog(callback: (Boolean) -> Unit) {
        val context = context ?: return

        val dialog = AlertDialog.Builder(context)
                .setMessage(R.string.wanna_delete_messages)
                .setNeutralButton(R.string.delete_for_all) { _, _ -> callback.invoke(true) }
                .setPositiveButton(R.string.delete_only_for_me) { _, _ -> callback.invoke(false) }
                .setNegativeButton(R.string.cancel, null)
                .create()
        dialog.show()
        dialog.stylize()
    }

    private fun showEditMessageDialog(message: Message2) {
        val context = context ?: return

        if (message.isOut() && time() - message.date < 3600 * 24) {
            TextInputAlertDialog(context, "", message.text) {
//                presenter.editMessage(message.id, it)
            }.show()
        } else {
            showError(context, R.string.unable_to_edit_message)
        }
    }

    /**
     * handles click on attached media
     */
    private fun onAttachClicked(attachment: Attachment) {
        when (attachment.type) {
            Attachment.TYPE_PHOTO -> attachment.photo?.let {
                ImageViewerActivity.viewImages(context, arrayListOf(it))
            }
            Attachment.TYPE_VIDEO -> attachment.video?.let {
//                apiUtils.openVideo(safeContext, it)
            }
        }
    }

    /**
     * handle setting subtitle and changing user's status
     */
    private fun onOnlineChanged(isOnline: Boolean, timeStamp: Int = 0) {
        chatToolbarController.setSubtitle(when {
            peerId.matchesChatId() -> getString(R.string.conversation)
            peerId.matchesGroupId() -> getString(R.string.community)
            else -> {
                val time = if (timeStamp == 0) {
                    time() - (if (isOnline) 0 else 300)
                } else {
                    timeStamp
                }
                val stringRes = if (isOnline) R.string.online_seen else R.string.last_seen
                getString(stringRes, getTime(time, withSeconds = Prefs.showSeconds))
            }
        })
    }

    private fun onAttachmentsSelected(attachments: List<Attachment>) {
        attachedAdapter.addAll(attachments.toMutableList())
    }

    private fun onImagesSelected(paths: List<String>) {
        paths.forEach {
//            presenter.attachPhoto(it, context = context)
            inputController.addItemAsBeingLoaded(it)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ChatFragment.REQUEST_ATTACH -> {
                data?.extras?.apply {
                    getParcelableArrayList<Attachment>(AttachFragment.ARG_ATTACHMENTS)
                            ?.let(::onAttachmentsSelected)
                    getStringArrayList(AttachFragment.ARG_PATHS)
                            ?.let(::onImagesSelected)
                }
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        chatToolbarController.hideActions()
    }

    override fun getAdapterSettings() = MessagesAdapter.Settings(
            isImportant = false
    )

    override fun getAdapterCallback() = AdapterCallback()

    companion object {

        const val ARG_PEER_ID = "peerId"
        const val ARG_TITLE = "title"
        const val ARG_FORWARDED = "forwarded"
        const val ARG_PHOTO = "photo"
        const val ARG_SHARE_TEXT = "shareText"
        const val ARG_SHARE_IMAGE = "shareImage"

        fun newInstance(dialog: Dialog, forwarded: String? = null,
                        shareText: String? = null, shareImage: String? = null): ChatMessagesFragment {
            val fragment = ChatMessagesFragment()
            fragment.arguments = Bundle().apply {
                putInt(ARG_PEER_ID, dialog.peerId)
                putString(ARG_TITLE, dialog.alias ?: dialog.title)
                putString(ARG_PHOTO, dialog.photo)
                if (!forwarded.isNullOrEmpty()) {
                    putString(ARG_FORWARDED, forwarded)
                }
                if (!shareText.isNullOrEmpty()) {
                    putString(ARG_SHARE_TEXT, shareText)
                }
                if (!shareImage.isNullOrEmpty()) {
                    putString(ARG_SHARE_IMAGE, shareImage)
                }
            }
            return fragment
        }
    }

    inner class AdapterCallback : MessagesAdapter.Callback {
        override fun onClicked(message: Message2) {
            getContextPopup(context ?: return, R.layout.popup_message) {
                when (it.id) {
                    R.id.llCopy -> copyToClip(message.text)
                    R.id.llEdit -> showEditMessageDialog(message)
                    R.id.llReply -> {
                        attachedAdapter.fwdMessages = "${message.id}"
                        attachedAdapter.isReply = true
                    }
                    R.id.llForward -> {
                        DialogsForwardActivity.launch(context, forwarded = "${message.id}")
                    }
                    R.id.llDelete -> {
                        val callback = { forAll: Boolean ->
//                            presenter.deleteMessages(mutableListOf(message.id), forAll)
                        }
                        if (message.isOut() && time() - message.date < 3600 * 24) {
                            showDeleteMessagesDialog(callback)
                        } else {
                            context?.let {
                                showDeleteDialog(it) { callback.invoke(false) }
                            }
                        }
                    }
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

    private inner class InputCallback : ChatInputController.ChatInputCallback {

        override fun onStickerClicked(sticker: Sticker) {
//            presenter.sendSticker(sticker)
        }

        override fun onSendClick() {
//            onSend(etInput.asText())
        }

        override fun hasMicPermissions(): Boolean {
            val hasPermissions = permissionHelper.hasRecordAudioPermissions()
            if (!hasPermissions) {
                permissionHelper.request(arrayOf(PermissionHelper.RECORD_AUDIO)) {}
            }
            return hasPermissions
        }

        override fun onAttachClick() {
            AttachActivity.launch(this@ChatMessagesFragment, ChatFragment.REQUEST_ATTACH)
        }

        override fun onTypingInvoke() {
//            presenter.setTyping()
        }

        override fun onVoiceVisibilityChanged(visible: Boolean) {
            rlRecord.setVisible(visible)
        }

        override fun onVoiceTimeUpdated(time: Int) {
            tvRecord.text = secToTime(time)
            if (time % 5 == 1) {
//                presenter.setAudioMessaging()
            }
        }

        override fun onVoiceRecorded(fileName: String) {
            inputController.addItemAsBeingLoaded(fileName)
//            presenter.attachVoice(fileName)
        }

        override fun onVoiceError(error: String) {
            showError(context, error)
        }
    }
}