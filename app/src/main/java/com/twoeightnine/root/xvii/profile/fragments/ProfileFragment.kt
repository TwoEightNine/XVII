package com.twoeightnine.root.xvii.profile.fragments

import android.graphics.Color
import androidx.annotation.StringRes
import android.view.View
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.fragments.ChatFragment
import com.twoeightnine.root.xvii.dagger.ApiService
import com.twoeightnine.root.xvii.fragments.BaseFragment
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.model.Message
import com.twoeightnine.root.xvii.model.Photo
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.mvp.presenter.ProfileFragmentPresenter
import com.twoeightnine.root.xvii.mvp.view.ProfileFragmentView
import com.twoeightnine.root.xvii.utils.*
import com.twoeightnine.root.xvii.views.photoviewer.ImageViewerActivity
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.item_user_field.view.*
import javax.inject.Inject

class ProfileFragment : BaseFragment(), ProfileFragmentView {

    private var userId = 0

    @Inject
    lateinit var api: ApiService

    private lateinit var presenter: ProfileFragmentPresenter

    companion object {

        fun newInstance(userId: Int): ProfileFragment {
            val fragment = ProfileFragment()
            fragment.userId = userId
            return fragment
        }

    }

    override fun bindViews(view: View) {
        super.bindViews(view)
        stylize()
    }

    override fun onNew(view: View) {
        super.onNew(view)
        App.appComponent?.inject(this)
        presenter = ProfileFragmentPresenter(api, userId)
        presenter.view = this
        presenter.loadUser()
    }

    private fun stylize() {
        if (Prefs.isNight) {
            Style.forViewGroupColor(rlBack)
            llHeader.setBackgroundColor(Color.WHITE)
            llContainer.setBackgroundColor(Color.WHITE)
            Style.forViewGroup(llCounters)
            Style.forViewGroup(rlChat)
        }
    }

    override fun getLayout() = R.layout.fragment_profile

    override fun showLoading() {
        loader.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        loader.visibility = View.INVISIBLE
    }

    override fun showError(error: String) {
        showError(activity, error)
    }

    override fun onPhotosLoaded(photos: MutableList<Photo>) {
        ImageViewerActivity.viewImages(safeActivity, photos)
    }

    override fun onFoafLoaded(date: String) {
        add(R.string.registration_date, formatDate(date).toLowerCase())
    }

    override fun onUserLoaded(user: User) {
        CacheHelper.saveUserAsync(user)
        civPhoto.load(user.photoMax)
        civPhoto.setOnClickListener { presenter.loadProfilePhotos() }
        tvName.text = user.fullName
        rlChat.setOnClickListener {
            rootActivity.loadFragment(ChatFragment.newInstance(Message(
                    0, 0, userId, 0, 0, user.fullName, "", null
            )))
        }
        if (!user.deactivated.isNullOrEmpty()) return
        tvLastSeen.text = getString(R.string.last_seen, getTime(user.lastSeen?.time
                ?: 0, full = true))
        ivOnline.visibility = if ((user.online ?: 0) == 1) View.VISIBLE else View.GONE
        add(R.string.link, user.link, { /*goTo(user.getLink())*/ }) { copy(user.link, R.string.link) }
        add(R.string.id, "${user.id}", null) { copy("${user.id}", R.string.id) }
        add(R.string.status, user.status, null) { copy(user.status, R.string.status) }
        add(R.string.bdate, formatDate(formatBdate(user.bdate)).toLowerCase())
        if (user.city != null) {
            add(R.string.city, user.city.title)
        }
        add(R.string.hometown, user.hometown)
        add(R.string.relation, getRelation(safeActivity, user.relation ?: 0))
        add(R.string.mphone, user.mobilePhone,
                {
                    callIntent(safeActivity, user.mobilePhone ?: "")
                }) { copy(user.mobilePhone, R.string.mphone) }
        add(R.string.hphone, user.homePhone,
                {
                    callIntent(safeActivity, user.homePhone ?: "")
                }) { copy(user.homePhone, R.string.hphone) }
        add(R.string.facebook, user.facebook, null) { copy(user.facebook, R.string.facebook) }
        add(R.string.site, user.site, { goTo(user.site) }) { copy(user.site, R.string.site) }
        add(R.string.twitter, user.twitter, null) { copy(user.twitter, R.string.twitter) }
        add(R.string.instagram, user.instagram, null) { copy(user.instagram, R.string.instagram) }
        add(R.string.skype, user.skype, null) { copy(user.skype, R.string.skype) }
        tvFriendsCOunt.text = shortifyNumber(user.counters?.friends ?: 0)
        tvFollowersCount.text = shortifyNumber(user.counters?.followers ?: 0)
    }

    private fun copy(text: String?, title: Int) {
        copyToClip(text ?: return)
        showCommon(activity, getString(R.string.copied, getString(title)))
    }

    private fun goTo(url: String?) {
        simpleUrlIntent(safeActivity, url ?: return)
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
}