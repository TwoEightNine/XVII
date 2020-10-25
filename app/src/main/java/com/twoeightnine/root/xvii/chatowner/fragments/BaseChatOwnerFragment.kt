package com.twoeightnine.root.xvii.chatowner.fragments

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import androidx.annotation.DrawableRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.chatowner.ChatOwnerViewModel
import com.twoeightnine.root.xvii.chatowner.model.ChatOwner
import com.twoeightnine.root.xvii.chats.messages.chat.usual.ChatActivity
import com.twoeightnine.root.xvii.lg.L
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.model.attachments.Photo
import com.twoeightnine.root.xvii.photoviewer.ImageViewerActivity
import com.twoeightnine.root.xvii.utils.*
import com.twoeightnine.root.xvii.views.RateAlertDialog
import kotlinx.android.synthetic.main.fragment_chat_owner.ivAvatar
import kotlinx.android.synthetic.main.fragment_chat_owner.nsvContent
import kotlinx.android.synthetic.main.fragment_chat_owner.tvInfo
import kotlinx.android.synthetic.main.fragment_chat_owner.tvTitle
import kotlinx.android.synthetic.main.fragment_chat_owner_user.*
import kotlinx.android.synthetic.main.item_chat_owner_field.view.*

abstract class BaseChatOwnerFragment<T : ChatOwner> : BaseFragment() {

    private val peerId by lazy {
        arguments?.getInt(ARG_PEER_ID) ?: 0
    }
    private var chatOwner: ChatOwner? = null

    protected lateinit var viewModel: ChatOwnerViewModel

    abstract fun getChatOwnerClass(): Class<T>

    abstract fun bindChatOwner(chatOwner: T?)

    abstract fun getBottomPaddableView(): View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val behavior = BottomSheetBehavior.from(nsvContent)
        behavior.setBottomSheetCallback(ProfileBottomSheetCallback(activity ?: return))
        fabOpenChat.setOnClickListener {
            chatOwner?.also {
                ChatActivity.launch(context, it)
            }
        }
        ivAvatar?.setOnClickListener(::onAvatarClicked)
        ivAvatarHighRes?.setOnClickListener(::onAvatarClicked)
        context?.let { RateAlertDialog(it).show() }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProviders.of(this)[ChatOwnerViewModel::class.java]
        viewModel.chatOwner.observe(viewLifecycleOwner, Observer(::onChatOwnerLoaded))
        viewModel.photos.observe(viewLifecycleOwner, Observer(::onPhotosLoaded))
        viewModel.loadChatOwner(peerId, getChatOwnerClass())

        swNotifications.stylize()
        fabOpenChat.stylize()
        progressBar?.stylize()

        getBottomPaddableView().setBottomInsetMargin()
    }

    @Suppress("UNCHECKED_CAST")
    protected fun getChatOwner(): T? = viewModel.chatOwner.value?.data as? T

    @Suppress("UNCHECKED_CAST")
    private fun onChatOwnerLoaded(data: Wrapper<ChatOwner>) {
        if (data.data != null) {
            rlLoader.hide()
            chatOwner = data.data
            chatOwner?.apply {
                ivAvatar?.load(getAvatar())
                tvTitle.text = getTitle()
                xviiToolbar?.title = getTitle()
                context?.also {
                    tvInfo.text = getInfoText(it)
                    getPrivacyInfo(it).also { privacyInfo ->
                        ivWarning.setVisible(privacyInfo != null)
                        tvPrivacy.setVisible(privacyInfo != null)
                        privacyInfo?.also { tvPrivacy.text = it }
                    }
                }
                if (Prefs.lowerTexts) {
                    tvTitle.lower()
                    tvInfo.lower()
                }
                swNotifications.isChecked = viewModel.getShowNotifications(getPeerId())
                resetValues()
                bindChatOwner(this as? T)
                viewModel.loadPhotos(getPeerId(), getChatOwnerClass())
            }
        } else {
            showError(context, data.error ?: "")
        }
    }

    private fun onPhotosLoaded(photos: List<Photo>) {
        if (photos.isNotEmpty()) {
            ivAvatar?.postDelayed({
                loadHighResWithAnimation(photos[0].getOptimalPhoto()?.url)
            }, 1000L)
        }
    }

    private fun onAvatarClicked(v: View) {
        viewModel.photos.value?.also { photos ->
            if (photos.isNotEmpty()) {
                ImageViewerActivity.viewImages(context, ArrayList(photos))
            }
        }
    }

    private fun resetValues() {
        llContainer.removeAllViews()
    }

    protected fun addValue(
            @DrawableRes icon: Int,
            text: String?,
            onClick: ((String) -> Unit)? = null,
            onLongClick: ((String) -> Unit)? = null
    ) {
        if (text.isNullOrBlank()) return

        with(View.inflate(context, R.layout.item_chat_owner_field, null)) {
            if (icon != 0) {
                ivIcon.setImageResource(icon)
                ivIcon.stylize()
            }
            tvValue.text = text.toLowerCase()
            onClick?.also {
                rlItem.setOnClickListener { onClick(text) }
            }
            onLongClick?.also {
                rlItem.setOnLongClickListener { onLongClick(text); true }
            }
            llContainer.addView(this)
        }
    }

    protected fun copy(text: String?, title: Int) {
        copyToClip(text ?: return)
        showToast(activity, getString(R.string.copied, getString(title)))
    }

    protected fun goTo(url: String?) {
        simpleUrlIntent(context, url)
    }

    private fun loadHighResWithAnimation(url: String?) {
        if (url == null) return

        Picasso.get()
                .load(url)
                .into(object : Target {
                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}

                    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}

                    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                        ivAvatarHighRes?.setImageBitmap(bitmap)
                        ivAvatarHighRes?.fadeIn(700L) {
                            ivAvatar?.hide()
                        }
                    }
                })
    }

    override fun onDestroyView() {
        chatOwner?.getPeerId()?.also { peerId ->
            viewModel.setShowNotifications(peerId, swNotifications.isChecked)
        }
        super.onDestroyView()
    }

    companion object {

        const val ARG_PEER_ID = "peerId"
        const val PARALLAX_COEFF = 0.5f
    }

    private inner class ProfileBottomSheetCallback(activity: Activity) : BottomSheetBehavior.BottomSheetCallback() {

        private var toolbarColored = false
        private val imageHeight = resources.getDimensionPixelSize(R.dimen.profile_avatar_height)
        private val screenHeight = activity.let {
            val displayMetrics = DisplayMetrics()
            it.windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.heightPixels
        }

        override fun onSlide(p0: View, offset: Float) {
            L.def().log("offset = $offset")
            when {
                offset <= 0f -> return
                offset == 1f -> if (!toolbarColored && shouldColorToolbar()) {
                    toolbarColored = true
                    setStatusBarLight(isLight = true)
                }
                toolbarColored -> {
                    toolbarColored = false
                    setStatusBarLight(isLight = false)
                }
                else -> {
                    val margin = imageHeight * -offset * PARALLAX_COEFF
                    (ivAvatar?.layoutParams as? CoordinatorLayout.LayoutParams)?.apply {
                        topMargin = margin.toInt()
                        ivAvatar?.layoutParams = this
                    }
                    (ivAvatarHighRes?.layoutParams as? CoordinatorLayout.LayoutParams)?.apply {
                        topMargin = margin.toInt()
                        ivAvatarHighRes?.layoutParams = this
                    }
                }
            }
        }

        override fun onStateChanged(p0: View, state: Int) {

        }

        private fun shouldColorToolbar() = xviiToolbar
                ?.let { (screenHeight - it.height + 92) <= cvInfo.height } ?: false
    }
}