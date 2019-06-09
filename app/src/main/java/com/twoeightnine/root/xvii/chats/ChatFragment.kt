package com.twoeightnine.root.xvii.chats

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.BuildConfig
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.attachments.attach.AttachActivity
import com.twoeightnine.root.xvii.chats.attachments.attach.AttachFragment
import com.twoeightnine.root.xvii.chats.attachments.attached.AttachedAdapter
import com.twoeightnine.root.xvii.chats.attachments.attachments.AttachmentsActivity
import com.twoeightnine.root.xvii.chats.tools.ChatInputController
import com.twoeightnine.root.xvii.chats.tools.ChatToolbarController
import com.twoeightnine.root.xvii.dialogs.activities.DialogsForwardActivity
import com.twoeightnine.root.xvii.dialogs.models.Dialog
import com.twoeightnine.root.xvii.fragments.BaseOldFragment
import com.twoeightnine.root.xvii.lg.Lg
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.model.Message
import com.twoeightnine.root.xvii.model.attachments.*
import com.twoeightnine.root.xvii.mvp.presenter.ChatFragmentPresenter
import com.twoeightnine.root.xvii.mvp.view.ChatFragmentView
import com.twoeightnine.root.xvii.photoviewer.ImageViewerActivity
import com.twoeightnine.root.xvii.profile.activities.ProfileActivity
import com.twoeightnine.root.xvii.utils.*
import com.twoeightnine.root.xvii.views.FingerPrintAlertDialog
import com.twoeightnine.root.xvii.views.LoadingDialog
import com.twoeightnine.root.xvii.views.TextInputAlertDialog
import kotlinx.android.synthetic.main.chat_input_panel.*
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.toolbar_chat.*
import javax.inject.Inject

class ChatFragment : BaseOldFragment(), ChatFragmentView {

    @Inject
    lateinit var presenter: ChatFragmentPresenter

    @Inject
    lateinit var apiUtils: ApiUtils

    private val peerId by lazy { arguments?.getInt(ARG_PEER_ID) ?: 0 }
    private val title by lazy { arguments?.getString(ARG_TITLE) ?: "" }
    private val photo by lazy { arguments?.getString(ARG_PHOTO) ?: "" }
    private val forwardedMessages by lazy { arguments?.getString(ARG_FORWARDED) }
    private val shareText by lazy { arguments?.getString(ARG_SHARE_TEXT) }
    private val shareImage by lazy { arguments?.getString(ARG_SHARE_IMAGE) }

    private val permissionHelper by lazy { PermissionHelper(this) }
    private val attachedAdapter by lazy {
        AttachedAdapter(safeContext, ::onAttachClicked, inputController::setAttachedCount)
    }
    private val chatToolbarController by lazy {
        ChatToolbarController(toolbar)
    }

    private var dialogLoading: LoadingDialog? = null
    private lateinit var inputController: ChatInputController
    private lateinit var adapter: ChatAdapter

    private val handler = Handler()

    override fun getLayout() = R.layout.fragment_chat

    override fun bindViews(view: View) {
        inputController = ChatInputController(safeContext, view, InputCallback())
        swipeContainer.setOnRefreshListener { presenter.loadHistory(withClear = true) }

        initAdapters()
        initMultiAction()

        App.appComponent?.inject(this)
        try {
            presenter.view = this
            presenter.peerId = peerId
            presenter.initCrypto(context)
            presenter.subscribe()
        } catch (e: Exception) {
            Lg.i("bindViews: " + e.message)
            if (BuildConfig.DEBUG) {
                showError(activity, "" + e.message)
            }
            restartApp()
        }
        stylize()
        initContent()
    }

    override fun onNew(view: View) {
        try {
            if (isOnline()) {
                swipeContainer?.isRefreshing = true
                presenter.loadHistory(withClear = true)
            } else {
                swipeContainer?.isRefreshing = false
                showError(context, R.string.no_internet)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            restartApp()
        }
    }

    override fun onRecovered(view: View) {
        adapter.stopLoading(presenter.getSaved(), true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        try {
            chatToolbarController.setTitle(title)
            if (peerId != -App.GROUP) {
                chatToolbarController.setAvatar(photo)
            }
            if (!peerId.matchesUserId()) {
                onChangeOnline(false)
            }
            toolbar?.setOnClickListener {
                hideKeyboard(safeActivity)
                if (peerId.matchesUserId()) {
                    ProfileActivity.launch(context, peerId)
                }
            }
        } catch (e: UninitializedPropertyAccessException) {
            restartApp()
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

    private fun initAdapters() {
        adapter = ChatAdapter(safeActivity, ::loadMore, AdapterCallback(), ChatAdapter.ChatAdapterSettings(
                isImportant = false
        ))
        adapter.trier = { loadMore(adapter.itemCount) }
        adapter.multiListener = rlMultiAction::setVisible
        val llm = LinearLayoutManager(activity)
        llm.stackFromEnd = true
        rvChatList.layoutManager = llm
        rvChatList.adapter = adapter
        rvChatList.itemAnimator = null

        fabHasMore.setOnClickListener { rvChatList.scrollToPosition(adapter.itemCount - 1) }
        rvChatList.setOnScrollListener(ListScrollListener())

        rvAttached.layoutManager = LinearLayoutManager(context, LinearLayout.HORIZONTAL, false)
        rvAttached.adapter = attachedAdapter
    }

    private fun initMultiAction() {
        ivCancelMulti.setOnClickListener {
            adapter.clearMultiSelect()
        }
        ivMenuMulti.setOnClickListener { showMultiSelectPopup() }
        ivForwardMulti.setOnClickListener {
            DialogsForwardActivity.launch(context, forwarded = getSelectedMessageIds())
        }
        ivReplyMulti.setOnClickListener {
            attachedAdapter.fwdMessages = getSelectedMessageIds()
            adapter.clearMultiSelect()
        }
    }

    private fun getSelectedMessageIds() = adapter.multiSelect.map { it.id }.joinToString(separator = ",")

    private fun loadMore(offset: Int) {
        if (isOnline()) {
            presenter.loadHistory(offset)
        }
    }

    private fun showMultiSelectPopup() {
        val selectedList = adapter.multiSelect.map { it.id }.toMutableList()
        getContextPopup(safeActivity, R.layout.popup_message_multiselect) {
            when (it.id) {
                R.id.llDecrypt -> {
                    decrypt(selectedList)
                    adapter.clearMultiSelect()
                }
                R.id.llDelete -> {
                    showDeleteMessagesDialog {
                        // i haven't found how to make copy
                        val mids = MutableList(selectedList.size) { selectedList[it] }
                        presenter.deleteMessages(mids, it)
                        adapter.clearMultiSelect()
                    }
                }
                R.id.llMarkImportant -> {
                    presenter.markAsImportant(selectedList, 1)
                    adapter.clearMultiSelect()
                }
            }
        }.show()
    }

    override fun onAttachmentsSent() {
        attachedAdapter.clear()
    }

    private fun showDeleteMessagesDialog(callback: (Boolean) -> Unit) {
        val dialog = AlertDialog.Builder(safeContext)
                .setMessage(R.string.wanna_delete_messages)
                .setNeutralButton(R.string.delete_for_all) { _, _ -> callback.invoke(true) }
                .setPositiveButton(R.string.delete_only_for_me) { _, _ -> callback.invoke(false) }
                .setNegativeButton(R.string.cancel, null)
                .create()
        dialog.show()
        dialog.stylize()
    }

    private fun showEditMessageDialog(message: Message) {
        if (message.isOut && time() - message.date < 3600 * 24) {
            TextInputAlertDialog(safeContext, "", message.body) {
                presenter.editMessage(message.id, it)
            }.show()
        } else {
            showError(context, R.string.unable_to_edit_message)
        }
    }

    private fun decrypt(mids: MutableList<Int>) {
        adapter.items
                .filter { it.id in mids }
                .forEach { it.body = getDecrypted(it.body) }
        adapter.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.clear()
        inflater?.inflate(R.menu.menu_chat, menu)
        menu?.findItem(R.id.menu_fingerprint)?.isVisible = peerId.matchesUserId()
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        menu?.findItem(R.id.menu_encrypt)?.isChecked = presenter.isEncrypted
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.menu_encrypt -> {
                item.isChecked = !item.isChecked
                presenter.isEncrypted = item.isChecked
                showToast(activity, if (item.isChecked) R.string.enc_on else R.string.enc_off)
                true
            }
            R.id.menu_keys -> {
                hideKeyboard(safeActivity)
                showKeysDialog()
                true
            }
            R.id.menu_attachments -> {
                AttachmentsActivity.launch(context, peerId)
                true
            }
            R.id.menu_fingerprint -> {
                val fingerprint = presenter.cryptoEngine.getFingerPrint()
                val keyType = presenter.cryptoEngine.keyType
                FingerPrintAlertDialog(safeContext, fingerprint, keyType).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showKeysDialog() {
        getContextPopup(safeActivity, R.layout.popup_keys) {
            when (it.id) {
                R.id.llRandomKey -> {
                    if (peerId.matchesUserId()) {
                        presenter.isEncrypted = false
                        safeActivity.invalidateOptionsMenu()
                        keyGenerationHint()
                    } else {
                        showError(activity, R.string.no_exchg_in_chats)
                    }
                }
                R.id.llUserKey -> showKeyInputDialog()
                R.id.llDefaultKey -> {
                    presenter.setDefaultKey()
                    showToast(activity, R.string.key_reset)
                }
            }
        }.show()
    }

    private fun showKeyInputDialog() {
        TextInputAlertDialog(
                safeActivity,
                getString(R.string.user_key), "") {
            presenter.setUserKey(it)
            presenter.isEncrypted = true
            safeActivity.invalidateOptionsMenu()
            showToast(activity, getString(R.string.key_set))
        }.show()
    }

    private fun onSend(text: String) {
        if (text.isNotEmpty() || attachedAdapter.count > 0) {
            presenter.send(text, attachedAdapter.fwdMessages, attachedAdapter.asString())
            etInput.setText("")
        }
    }

    private fun onAttachmentsSelected(attachments: List<Attachment>) {
        attachedAdapter.addAll(attachments.toMutableList())
    }

    private fun onImagesSelected(paths: List<String>) {
        paths.forEach {
            presenter.attachPhoto(it, context = context)
            inputController.addItemAsBeingLoaded(it)
        }
    }

    private fun onAttachClicked(attachment: Attachment) {
        when (attachment.type) {
            Attachment.TYPE_PHOTO -> attachment.photo?.let {
                ImageViewerActivity.viewImages(context, arrayListOf(it))
            }
            Attachment.TYPE_VIDEO -> attachment.video?.let {
                apiUtils.openVideo(safeContext, it)
            }
        }
    }

    override fun onVoiceUploaded(path: String) {
        inputController.removeItemAsLoaded(path)
    }

    private fun getDecrypted(text: String): String {
        val cipherResult = presenter.cryptoEngine.decrypt(text)
        return if (cipherResult.verified && cipherResult.bytes != null) {
            getString(R.string.decrypted, String(cipherResult.bytes))
        } else {
            ""
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.isShown = true
        if (Prefs.markAsRead && adapter.items.isNotEmpty()) {
            presenter.markAsRead(adapter.items[adapter.items.size - 1].id)
        }
    }

    override fun onPause() {
        super.onPause()
        presenter.isShown = false
    }

    override fun showLoading() {
        adapter.startLoading()
    }

    override fun hideLoading() {
        adapter.stopLoading()
    }

    override fun showError(error: String) {
        adapter.setErrorLoading()
        showError(activity, error)
        swipeContainer?.isRefreshing = false
        Lg.wtf("in chat error: $error")
    }

    override fun onPhotoUploaded(path: String, attachment: Attachment) {
        inputController.removeItemAsLoaded(path)
        attachedAdapter.add(attachment)
    }

    override fun onHistoryLoaded(history: MutableList<Message>) {
        val needToScroll = adapter.itemCount < 2
        adapter.stopLoading(history, true)
        swipeContainer?.isRefreshing = false

        if (needToScroll) {
            var unreadPos = adapter.itemCount - 1
            for (pos in adapter.items.indices.reversed()) {
                if (!adapter.items[pos].isRead &&
                        !adapter.items[pos].isOut) {
                    unreadPos = pos
                } else {
                    break
                }
            }
            rvChatList?.scrollToPosition(unreadPos)
        }
    }

    override fun onMessageAdded(message: Message) {
        if (presenter.isEncrypted && message.body.matchesXviiCipher()) {
            message.body = getDecrypted(message.body)
        }
        val wasAtEnd = adapter.isAtEnd
        val alreadyHere = adapter.items.count { it.id == message.id } > 0
        if (!alreadyHere) adapter.add(message)
        if (wasAtEnd) {
            rvChatList?.scrollToPosition(adapter.itemCount - 1)
        }
    }

    override fun onHistoryClear() {
        adapter.clear()
    }

    override fun onSentError(text: String) {
        etInput.setText(text)
    }

    override fun onShowTyping() {
        chatToolbarController.showTyping()
    }

    override fun onHideTyping() {
        chatToolbarController.hideActions()
    }

    override fun onShowRecordingVoice() {
        chatToolbarController.showRecording()
    }

    override fun onHideRecordingVoice() {
        chatToolbarController.hideActions()
    }

    override fun onChangeOnline(isOnline: Boolean, timeStamp: Int) {
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

    override fun onReadOut(mid: Int) {
        for (position in adapter.items.indices) {
            val message = adapter.items[position]
            if (message.id <= mid && !message.isRead) {
                message.isRead = true
                adapter.notifyItemChanged(position)
            }
        }
    }

    override fun onMessagesDeleted(mids: MutableList<Int>) {
        adapter.items
                .filter { it.id in mids }
                .forEach { adapter.remove(it) }

    }

    override fun onMessageEdited(mid: Int, newText: String) {
        for (pos in adapter.items.indices) {
            if (adapter.items[pos].id == mid) {
                val mess = adapter.items[pos]
                mess.body = newText
                adapter.update(pos, mess)
            }
        }
    }

    private fun keyGenerationHint() {
        val alertDialog = AlertDialog.Builder(safeContext)
                .setMessage(R.string.generation_dh_hint)
                .setPositiveButton(R.string.ok, { _, _ -> presenter.startKeyExchange() })
                .create()
        alertDialog.show()
        alertDialog.stylize()
    }

    override fun onKeyGenerating() {
        dialogLoading?.dismiss()
        dialogLoading = LoadingDialog(
                safeActivity,
                getString(R.string.generating_keys),
                false
        )
        dialogLoading?.show()
    }

    override fun onKeySent() {
        dialogLoading?.dismiss()
    }

    override fun onKeyReceived(key: String, isWaiting: Boolean) {
        if (isWaiting) {
            presenter.finishKeyExchange(key)
        } else {
            presenter.supportKeyExchange(key)
        }
    }

    override fun onKeysExchanged() {
        presenter.isEncrypted = true
        safeActivity.invalidateOptionsMenu()
        showToast(activity, R.string.key_exchanged)
    }

    override fun onKeyExchangeFailed() {
        showError(context, R.string.key_xchg_failed)
    }

    override fun onCacheRestored() {

    }

    override fun onDetach() {
        super.onDetach()
        chatToolbarController.hideActions()
        presenter.unsubscribe()
    }

    override fun onBackPressed() = false

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_ATTACH -> {
                data?.extras?.apply {
                    getParcelableArrayList<Attachment>(AttachFragment.ARG_ATTACHMENTS)
                            ?.let(::onAttachmentsSelected)
                    getStringArrayList(AttachFragment.ARG_PATHS)
                            ?.let(::onImagesSelected)
                }
            }
        }
    }

    companion object {

        const val ARG_PEER_ID = "peerId"
        const val ARG_TITLE = "title"
        const val ARG_FORWARDED = "forwarded"
        const val ARG_PHOTO = "photo"
        const val ARG_SHARE_TEXT = "shareText"
        const val ARG_SHARE_IMAGE = "shareImage"

        const val REQUEST_ATTACH = 7364

        fun newInstance(dialog: Dialog, forwarded: String? = null,
                        shareText: String? = null, shareImage: String? = null): ChatFragment {
            val fragment = ChatFragment()
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

    private inner class AdapterCallback : ChatAdapter.ChatAdapterCallback {

        override fun onClicked(message: Message) {
            adapter.multiSelect(message)
            adapter.notifyItemChanged(adapter.items.indexOf(message))
        }

        override fun onLongClicked(message: Message): Boolean {
            getContextPopup(safeActivity, R.layout.popup_message) {
                when (it.id) {
                    R.id.llCopy -> copyToClip(message.body)
                    R.id.llEdit -> showEditMessageDialog(message)
                    R.id.llReply -> {
                        attachedAdapter.fwdMessages = "${message.id}"
                    }
                    R.id.llForward -> {
                        DialogsForwardActivity.launch(context, forwarded = "${message.id}")
                    }
                    R.id.llDelete -> {
                        val callback = { forAll: Boolean ->
                            presenter.deleteMessages(mutableListOf(message.id), forAll)
                        }
                        if (message.isOut && time() - message.date < 3600 * 24) {
                            showDeleteMessagesDialog(callback)
                        } else {
                            showDeleteDialog(safeActivity) { callback.invoke(false) }
                        }
                    }
                    R.id.llDecrypt -> {
                        message.body = getDecrypted(message.body)
                        adapter.notifyItemChanged(adapter.items.indexOf(message))
                    }
                    R.id.llMarkImportant ->
                        presenter.markAsImportant(
                                mutableListOf(message.id),
                                1
                        )
                }
            }.show()
            return true
        }

        override fun onUserClicked(userId: Int) {
            ProfileActivity.launch(context, userId)
        }

        override fun onDocClicked(doc: Doc) {
            dialogLoading?.dismiss()
            dialogLoading = LoadingDialog(
                    safeContext,
                    safeContext.getString(R.string.decrypting_image)
            )
            dialogLoading?.show()
            presenter.decryptDoc(safeContext, doc) { verified, path ->
                dialogLoading?.dismiss()
                if (!path.isNullOrEmpty() && verified) {
                    ImageViewerActivity.viewImage(safeContext, "file://$path")
                } else {
                    showError(context, R.string.invalid_file)
                }
            }
        }

        override fun onPhotoClicked(photo: Photo) {
            apiUtils.showPhoto(safeActivity, photo.photoId, photo.accessKey)
        }

        override fun onVideoClicked(video: Video) {
            apiUtils.openVideo(safeActivity, video)
        }
    }

    private inner class InputCallback : ChatInputController.ChatInputCallback {

        override fun onRichContentAdded(filePath: String) {
            onImagesSelected(arrayListOf(filePath))
        }

        override fun onStickerClicked(sticker: Sticker) {
            presenter.sendSticker(sticker)
        }

        override fun onSendClick() {
            onSend(etInput.asText())
        }

        override fun hasMicPermissions(): Boolean {
            val hasPermissions = permissionHelper.hasRecordAudioPermissions()
            if (!hasPermissions) {
                permissionHelper.request(arrayOf(PermissionHelper.RECORD_AUDIO)) {}
            }
            return hasPermissions
        }

        override fun onAttachClick() {
            AttachActivity.launch(this@ChatFragment, REQUEST_ATTACH)
        }

        override fun onTypingInvoke() {
            presenter.setTyping()
        }

        override fun onVoiceVisibilityChanged(visible: Boolean) {
            rlRecord.setVisible(visible)
        }

        override fun onVoiceTimeUpdated(time: Int) {
            tvRecord.text = secToTime(time)
            if (time % 5 == 1) {
                presenter.setAudioMessaging()
            }
        }

        override fun onVoiceRecorded(fileName: String) {
            inputController.addItemAsBeingLoaded(fileName)
            presenter.attachVoice(fileName)
        }

        override fun onVoiceError(error: String) {
            showError(context, error)
        }
    }

    private inner class ListScrollListener : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            if (fabHasMore.visibility != View.VISIBLE &&
                    adapter.lastVisiblePosition() != adapter.itemCount - 1) {
                fabHasMore.show()
            } else if (fabHasMore.visibility != View.INVISIBLE
                    && adapter.lastVisiblePosition() == adapter.itemCount - 1) {
                fabHasMore.hide()
            }
        }
    }
}