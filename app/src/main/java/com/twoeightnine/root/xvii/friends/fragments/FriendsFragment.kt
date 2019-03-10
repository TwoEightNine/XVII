package com.twoeightnine.root.xvii.friends.fragments

import android.os.Bundle
import android.os.Handler
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import butterknife.BindView
import butterknife.ButterKnife
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.CommonPagerAdapter
import com.twoeightnine.root.xvii.fragments.BaseFragment
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.mvp.presenter.FriendsFragmentPresenter
import com.twoeightnine.root.xvii.mvp.view.FriendsFragmentView
import com.twoeightnine.root.xvii.utils.CacheHelper
import com.twoeightnine.root.xvii.utils.showCommon
import com.twoeightnine.root.xvii.utils.showError
import com.twoeightnine.root.xvii.views.LoaderView
import javax.inject.Inject

class FriendsFragment : BaseFragment(), FriendsFragmentView {

    @BindView(R.id.viewPager)
    lateinit var viewPager: androidx.viewpager.widget.ViewPager
    @BindView(R.id.tabs)
    lateinit var tabs: TabLayout
    @BindView(R.id.loader)
    lateinit var loader: ProgressBar


    private lateinit var pagerAdapter: CommonPagerAdapter

    @Inject
    lateinit var presenter: FriendsFragmentPresenter

    private var offset: Int = 0

    companion object {

        fun newInstance(): FriendsFragment {
            val frag = FriendsFragment()
            //
            return frag
        }

    }

    override fun bindViews(view: View) {
        super.bindViews(view)
        ButterKnife.bind(this, view)
        initAdapter()
        App.appComponent?.inject(this)
        presenter.view = this
        Style.forTabLayout(tabs)
    }

    override fun onNew(view: View) {
        presenter.loadFriends()
    }

    override fun onRecovered(view: View) {
        try {
            onFriendsLoaded(presenter.friends)
            onOnlineFriendsLoaded(presenter.online)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initAdapter() {
        pagerAdapter = CommonPagerAdapter(childFragmentManager)
        pagerAdapter.add(FriendsAllFragment.newInstance(
                { loadMore() },
                { presenter.createChat(it) }
        ), getString(R.string.friends))
        pagerAdapter.add(FriendsOnlineFragment.newInstance { loadMore() }, getString(R.string.online))
        viewPager.adapter = pagerAdapter
        tabs.setupWithViewPager(viewPager, true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        updateTitle(getString(R.string.fiends))
    }

    override fun getLayout() = R.layout.fragment_friends

    override fun showLoading() {
        try {
            (pagerAdapter.getItem(0) as FriendsAllFragment)
                    .adapter.startLoading()
            (pagerAdapter.getItem(1) as FriendsOnlineFragment)
                    .adapter.startLoading()
        } catch (e: UninitializedPropertyAccessException) {
            loader.visibility = View.VISIBLE
        }
    }

    override fun hideLoading() {
        loader.visibility = View.GONE
    }

    override fun showError(error: String) {
        if (error == "") {
            showError(activity, R.string.cannot_create_chat)
            return
        }
        showError(activity, error)
    }

    override fun onFriendsLoaded(friends: MutableList<User>) {
        offset += friends.size
        CacheHelper.saveUsersAsync(friends)
        try {
            (pagerAdapter.getItem(0) as FriendsAllFragment)
                    .adapter.stopLoading(friends)
        } catch (e: UninitializedPropertyAccessException) { }
    }

    override fun onOnlineFriendsLoaded(friends: MutableList<User>) {
        try {
            (pagerAdapter.getItem(1) as FriendsOnlineFragment)
                    .adapter.stopLoading(friends)
        } catch (e: UninitializedPropertyAccessException) { }
    }

    override fun onChatCreated() {
        showCommon(activity, R.string.chat_created)
    }

    override fun onUsersClear() {}

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.clear()
        inflater?.inflate(R.menu.user_list_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId){
            R.id.menu_search -> {
                rootActivity.loadFragment(SearchUsersFragment())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadMore() {
        presenter.loadFriends(offset)
    }
}