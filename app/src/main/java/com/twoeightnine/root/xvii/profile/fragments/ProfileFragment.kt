package com.twoeightnine.root.xvii.profile.fragments

import android.graphics.Color
import android.support.annotation.StringRes
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
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
import com.twoeightnine.root.xvii.views.LoaderView
import com.twoeightnine.root.xvii.views.photoviewer.ImageViewerActivity
import de.hdodenhof.circleimageview.CircleImageView
import javax.inject.Inject

class ProfileFragment : BaseFragment(), ProfileFragmentView {

    @BindView(R.id.rlArc)
    lateinit var rlArc: RelativeLayout
    @BindView(R.id.llHeader)
    lateinit var llHeader: LinearLayout
    @BindView(R.id.rlBack)
    lateinit var rlBack: RelativeLayout
    @BindView(R.id.civPhoto)
    lateinit var civPhoto: CircleImageView
    @BindView(R.id.llContainer)
    lateinit var llContainer: LinearLayout
    @BindView(R.id.loader)
    lateinit var loader: LoaderView
    @BindView(R.id.tvName)
    lateinit var tvName: TextView
    @BindView(R.id.tvLastSeen)
    lateinit var tvLastSeen: TextView
    @BindView(R.id.ivOnline)
    lateinit var ivOnline: ImageView
    @BindView(R.id.llCounters)
    lateinit var llCounters: LinearLayout
    @BindView(R.id.tvFriendsCOunt)
    lateinit var tvFriendsCount: TextView
    @BindView(R.id.tvFollowersCount)
    lateinit var tvFollowersCount: TextView
    @BindView(R.id.rlChat)
    lateinit var rlChat: RelativeLayout

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
        ButterKnife.bind(this, view)
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
        add(R.string.registration_date, date)
    }

    override fun onUserLoaded(user: User) {
        CacheHelper.saveUserAsync(user)
        civPhoto.loadPhoto(user.photoMax)
        civPhoto.setOnClickListener { presenter.loadProfilePhotos() }
        tvName.text = user.fullName()
        rlChat.setOnClickListener {
            rootActivity.loadFragment(ChatFragment.newInstance(Message(
                    0, 0, userId, 0, 0, user.fullName(), "", null
            )))
        }
        if (!user.deactivated.isNullOrEmpty()) return
        tvLastSeen.text = getString(R.string.last_seen, getTime(user.lastSeen?.time ?: 0, true))
        ivOnline.visibility = if ((user.online ?: 0) == 1) View.VISIBLE else View.GONE
        add(R.string.link, user.getLink(), { /*goTo(user.getLink())*/ }, {
            copy(user.getLink(), R.string.link)
            true
        })
        add(R.string.id, "${user.id}", null, {
            copy("${user.id}", R.string.id)
            true
        })
        add(R.string.status, user.status, null, {
            copy(user.status, R.string.status)
            true
        })
        add(R.string.bdate, user.bdate)
        if (user.city != null) {
            add(R.string.city, user.city?.title)
        }
        add(R.string.hometown, user.hometown)
        add(R.string.relation, getRelation(safeActivity, user.relation ?: 0))
        add(R.string.mphone, user.mobilePhone,
                { callIntent(safeActivity, user.mobilePhone ?: "") },
                {
                    copy(user.mobilePhone, R.string.mphone)
                    true
                })
        add(R.string.hphone, user.homePhone,
                { callIntent(safeActivity, user.homePhone ?: "") },
                {
                    copy(user.homePhone, R.string.hphone)
                    true
                })
        add(R.string.facebook, user.facebook, null, {
            copy(user.facebook, R.string.facebook)
            true
        })
        add(R.string.site, user.site, { goTo(user.site) }, {
            copy(user.site, R.string.site)
            true
        })
        add(R.string.twitter, user.twitter, null, {
            copy(user.twitter, R.string.twitter)
            true
        })
        add(R.string.instagram, user.instagram, null, {
            copy(user.instagram, R.string.instagram)
            true
        })
        add(R.string.skype, user.skype, null, {
            copy(user.skype, R.string.skype)
            true
        })
        tvFriendsCount.text = shortifyNumber(user.counters?.friends ?: 0)
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
            onLongClick: ((View) -> Boolean)? = null) {
        if (!value.isNullOrEmpty()) {
            val view = View.inflate(activity, R.layout.item_user_field, null)
            val binder = ViewBinder(view)
            binder.tvTitle.text = getString(title)
            binder.tvValue.text = value
            binder.rlItem.setOnClickListener(onClick)
            binder.rlItem.setOnLongClickListener(onLongClick)
            llContainer.addView(view)
        }
    }

    inner class ViewBinder(view: View) {

        @BindView(R.id.tvTitle)
        lateinit var tvTitle: TextView
        @BindView(R.id.tvValue)
        lateinit var tvValue: TextView
        @BindView(R.id.rlItem)
        lateinit var rlItem: RelativeLayout

        init {
            ButterKnife.bind(this, view)
        }
    }
}