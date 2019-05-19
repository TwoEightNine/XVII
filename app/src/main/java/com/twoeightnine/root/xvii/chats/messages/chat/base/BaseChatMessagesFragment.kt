package com.twoeightnine.root.xvii.chats.messages.chat.base

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
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
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.model.CanWrite
import com.twoeightnine.root.xvii.model.Message2
import com.twoeightnine.root.xvii.model.attachments.*
import com.twoeightnine.root.xvii.photoviewer.ImageViewerActivity
import com.twoeightnine.root.xvii.profile.activities.ProfileActivity
import com.twoeightnine.root.xvii.utils.*
import com.twoeightnine.root.xvii.views.TextInputAlertDialog
import com.twoeightnine.root.xvii.web.VideoViewerActivity
import kotlinx.android.synthetic.main.chat_input_panel.*
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.toolbar_chat.*

abstract class BaseChatMessagesFragment<VM : BaseChatMessagesViewModel> : BaseMessagesFragment<VM>() {

    protected val peerId by lazy { arguments?.getInt(ARG_PEER_ID) ?: 0 }
    protected val title by lazy { arguments?.getString(ARG_TITLE) ?: "" }
    protected val photo by lazy { arguments?.getString(ARG_PHOTO) ?: "" }
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
    private lateinit var inputController: ChatInputController

    abstract fun onEncryptedDocClicked(doc: Doc)

    override fun prepareViewModel() {
        viewModel.peerId = peerId
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        inputController = ChatInputController(contextOrThrow, view, InputCallback())
        swipeContainer.setOnRefreshListener { viewModel.loadMessages() }

        rvAttached.layoutManager = LinearLayoutManager(context, LinearLayout.HORIZONTAL, false)
        rvAttached.adapter = attachedAdapter
        stylize()
        initContent()
        initMultiSelectMenu()

        viewModel.getLastSeen().observe(this, Observer { onOnlineChanged(it.first, it.second) })
        viewModel.getCanWrite().observe(this, Observer { onCanWriteChanged(it) })
        viewModel.getActivity().observe(this, Observer { onActivityChanged(it) })
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

    private fun initMultiSelectMenu() {
        ivReplyMulti.setOnClickListener {
            attachedAdapter.fwdMessages = getSelectedMessageIds()
            attachedAdapter.isReply = true
            adapter.multiSelectMode = false
        }
        ivDeleteMulti.setOnClickListener {
            val selectedMessages = adapter.multiSelect
            val callback = { forAll: Boolean ->
                viewModel.deleteMessages(getSelectedMessageIds(), forAll)
                adapter.multiSelectMode = false
            }
            val edgeDate = time() - 3600 * 24
            val isOut = selectedMessages.filter { !it.isOut() }.isEmpty()
            val isRecent = selectedMessages.filter { it.date < edgeDate }.isEmpty()
            if (isOut && isRecent) {
                showDeleteMessagesDialog(callback)
            } else {
                context?.let {
                    showDeleteDialog(it) { callback.invoke(false) }
                }
            }
        }
        ivMarkMulti.setOnClickListener {
            viewModel.markAsImportant(getSelectedMessageIds())
            adapter.multiSelectMode = false
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

    override fun onResume() {
        super.onResume()
        viewModel.isShown = true
    }

    override fun onPause() {
        super.onPause()
        viewModel.isShown = false
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
                viewModel.editMessage(message.id, it)
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
                viewModel.loadVideo(context ?: return, it, { playerUrl ->
                    VideoViewerActivity.launch(context, playerUrl)
                }, { error ->
                    showError(context, error)
                })
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

    /**
     * handles setting peer's activity: one of [BaseChatMessagesViewModel.ACTIVITY_TYPING],
     * [BaseChatMessagesViewModel.ACTIVITY_VOICE], [BaseChatMessagesViewModel.ACTIVITY_NONE]
     */
    private fun onActivityChanged(activity: String) {
        when (activity) {
            BaseChatMessagesViewModel.ACTIVITY_VOICE -> chatToolbarController.showRecording()
            BaseChatMessagesViewModel.ACTIVITY_TYPING -> chatToolbarController.showTyping()
            BaseChatMessagesViewModel.ACTIVITY_NONE -> chatToolbarController.hideActions()
        }
    }

    private fun onCanWriteChanged(canWrite: CanWrite) {
        rlCantWrite.setVisible(!canWrite.allowed)
    }

    private fun onAttachmentsSelected(attachments: List<Attachment>) {
        attachedAdapter.addAll(attachments.toMutableList())
    }

    private fun onImagesSelected(paths: List<String>) {
        paths.forEach {
            viewModel.attachPhoto(it) { path, attachment ->
                inputController.removeItemAsLoaded(path)
                attachedAdapter.add(attachment)
            }
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
                            viewModel.deleteMessages(message.id.toString(), forAll)
                        }
                        if (message.isOut() && time() - message.date < 3600 * 24) {
                            showDeleteMessagesDialog(callback)
                        } else {
                            context?.let {
                                showDeleteDialog(it) { callback.invoke(false) }
                            }
                        }
                    }
                    R.id.llMarkImportant -> viewModel.markAsImportant(message.id.toString())
                }
            }.show()
        }

        override fun onUserClicked(userId: Int) {
            ProfileActivity.launch(context, userId)
        }

        override fun onEncryptedFileClicked(doc: Doc) {
            onEncryptedDocClicked(doc)
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

        override fun onRichContentAdded(filePath: String) {
            onImagesSelected(arrayListOf(filePath))
        }

        override fun onStickerClicked(sticker: Sticker) {
            viewModel.sendSticker(sticker, attachedAdapter.replyTo)
        }

        override fun onSendClick() {
            val replyTo = attachedAdapter.replyTo
            val forwarded = if (replyTo == null) {
                attachedAdapter.fwdMessages
            } else {
                null
            }
            viewModel.sendMessage(
                    text = etInput.asText(),
                    attachments = attachedAdapter.asString(),
                    forwardedMessages = forwarded,
                    replyTo = replyTo
            )
            etInput.clear()
            attachedAdapter.clear()
        }

        override fun hasMicPermissions(): Boolean {
            val hasPermissions = permissionHelper.hasRecordAudioPermissions()
            if (!hasPermissions) {
                permissionHelper.request(arrayOf(PermissionHelper.RECORD_AUDIO)) {}
            }
            return hasPermissions
        }

        override fun onAttachClick() {
            AttachActivity.launch(this@BaseChatMessagesFragment, ChatFragment.REQUEST_ATTACH)
        }

        override fun onTypingInvoke() {
            viewModel.setActivity(type = BaseChatMessagesViewModel.ACTIVITY_TYPING)
        }

        override fun onVoiceVisibilityChanged(visible: Boolean) {
            rlRecord.setVisible(visible)
        }

        override fun onVoiceTimeUpdated(time: Int) {
            tvRecord.text = secToTime(time)
            if (time % 5 == 1) {
                viewModel.setActivity(type = BaseChatMessagesViewModel.ACTIVITY_VOICE)
            }
        }

        override fun onVoiceRecorded(fileName: String) {
            inputController.addItemAsBeingLoaded(fileName)
            viewModel.attachVoice(fileName, inputController::removeItemAsLoaded)
        }

        override fun onVoiceError(error: String) {
            showError(context, error)
        }
    }
}