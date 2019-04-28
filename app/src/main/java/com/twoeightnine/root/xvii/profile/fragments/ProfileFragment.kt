package com.twoeightnine.root.xvii.profile.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.chats.ChatActivity
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.model.attachments.Photo
import com.twoeightnine.root.xvii.photoviewer.ImageViewerActivity
import com.twoeightnine.root.xvii.profile.viewmodels.ProfileViewModel
import com.twoeightnine.root.xvii.utils.*
import com.twoeightnine.root.xvii.views.RateAlertDialog
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.item_user_field.view.*
import javax.inject.Inject

class ProfileFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ProfileViewModel.Factory
    private lateinit var viewModel: ProfileViewModel

    private val userId by lazy { arguments?.getInt(ARG_USER_ID) ?: 0 }

    override fun getLayoutId() = R.layout.fragment_profile

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        stylize()
        App.appComponent?.inject(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory)[ProfileViewModel::class.java]
        viewModel.getUser().observe(this, Observer { updateUser(it) })
        viewModel.getFoaf().observe(this, Observer { updateFoaf(it) })
        viewModel.userId = userId
        viewModel.loadUser()

        context?.let { RateAlertDialog(it).show() }
    }

    private fun stylize() {
        if (Prefs.isLightTheme) {
            rlBack.stylizeColor()
            llHeader.setBackgroundColor(Color.WHITE)
            llContainer.setBackgroundColor(Color.WHITE)
            llCounters.stylize()
            rlChat.stylize()
        }
    }

    private fun updateUser(data: Wrapper<User>) {
        progressBar.hide()
        if (data.data != null) {
            bindUser(data.data)
        } else {
            showError(context, data.error ?: getString(R.string.error))
        }
    }

    private fun onPhotosLoaded(photos: ArrayList<Photo>) {
        ImageViewerActivity.viewImages(context, photos)
    }

    private fun updateFoaf(data: Wrapper<String>) {
        if (data.data != null) {
            add(R.string.registration_date, formatDate(data.data).toLowerCase())
        }
    }

    private fun bindUser(user: User) {
        llContainer.removeAllViews()
        civPhoto.load(user.photoMax)
        civPhoto.setOnClickListener { viewModel.loadPhotos(::onPhotosLoaded) }
        tvName.text = user.fullName
        rlChat.setOnClickListener { ChatActivity.launch(context, user) }
        if (!user.deactivated.isNullOrEmpty()) return
        val onlineRes = if (user.isOnline) R.string.online_seen else R.string.last_seen
        tvLastSeen.text = getString(onlineRes, getTime(user.lastSeen?.time ?: 0, full = true))

        add(R.string.link, user.link, { goTo(user.link) }) { copy(user.link, R.string.link) }
        add(R.string.id, "${user.id}", null) { copy("${user.id}", R.string.id) }
        add(R.string.status, user.status, null) { copy(user.status, R.string.status) }
        add(R.string.bdate, formatDate(formatBdate(user.bdate)).toLowerCase())
        if (user.city != null) {
            add(R.string.city, user.city.title)
        }
        add(R.string.hometown, user.hometown)
        add(R.string.relation, getRelation(context, user.relation))
        add(R.string.mphone, user.mobilePhone,
                {
                    callIntent(context, user.mobilePhone)
                }) { copy(user.mobilePhone, R.string.mphone) }
        add(R.string.hphone, user.homePhone,
                {
                    callIntent(context, user.homePhone)
                }) { copy(user.homePhone, R.string.hphone) }
        add(R.string.facebook, user.facebook, null) { copy(user.facebook, R.string.facebook) }
        add(R.string.site, user.site, { goTo(user.site) }) { copy(user.site, R.string.site) }
        add(R.string.twitter, user.twitter, { goTo("https://twitter.com/${user.instagram}") }) { copy(user.twitter, R.string.twitter) }
        add(R.string.instagram, user.instagram, { goTo("https://instagram.com/${user.instagram}") }) { copy(user.instagram, R.string.instagram) }
        add(R.string.skype, user.skype, null) { copy(user.skype, R.string.skype) }
        tvFriendsCOunt.text = shortifyNumber(user.counters?.friends ?: 0)
        tvFollowersCount.text = shortifyNumber(user.counters?.followers ?: 0)
    }

    private fun copy(text: String?, title: Int) {
        copyToClip(text ?: return)
        showToast(activity, getString(R.string.copied, getString(title)))
    }

    private fun goTo(url: String?) {
        simpleUrlIntent(context, url)
    }

    private fun add(@StringRes title: Int,
                    value: String?,
                    onClick: ((View) -> Unit)? = null,
                    onLongClick: ((View) -> Unit)? = null) {
        if (!value.isNullOrEmpty()) {
            val view = View.inflate(activity, R.layout.item_user_field, null)
            with(view) {
                tvTitle.text = getString(title)
                tvValue.text = value
                rlItem.setOnClickListener(onClick)
                rlItem.setOnLongClickListener {
                    onLongClick?.invoke(it)
                    true
                }
            }
            llContainer.addView(view)
        }
    }

    companion object {

        const val ARG_USER_ID = "userId"

        fun newInstance(userId: Int): ProfileFragment {
            val fragment = ProfileFragment()
            fragment.arguments = Bundle().apply {
                putInt(ARG_USER_ID, userId)
            }
            return fragment
        }

    }
}