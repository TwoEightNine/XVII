package com.twoeightnine.root.xvii.friends.fragments

import android.os.CountDownTimer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import butterknife.BindView
import butterknife.ButterKnife
import com.miguelcatalan.materialsearchview.MaterialSearchView
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.fragments.BaseFragment
import com.twoeightnine.root.xvii.friends.adapters.UsersAdapter
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.mvp.presenter.SearchUsersPresenter
import com.twoeightnine.root.xvii.mvp.view.SearchUsersFragmentView
import com.twoeightnine.root.xvii.profile.fragments.ProfileFragment
import com.twoeightnine.root.xvii.utils.CacheHelper
import com.twoeightnine.root.xvii.utils.hideKeyboard
import javax.inject.Inject

class SearchUsersFragment: BaseFragment(), SearchUsersFragmentView {

    @Inject
    lateinit var presenter: SearchUsersPresenter

    @BindView(R.id.rvUsers)
    lateinit var rvUsers: androidx.recyclerview.widget.RecyclerView
    @BindView(R.id.searchView)
    lateinit var searchView: MaterialSearchView

    private lateinit var adapter: UsersAdapter

    private var timer: CountDownTimer? = null

    override fun bindViews(view: View) {
        super.bindViews(view)
        ButterKnife.bind(this, view)
        initAdapter()
        initSearchView()
        App.appComponent?.inject(this)
        presenter.view = this
    }

    override fun onNew(view: View) {
        presenter.searchUsers()
    }

    override fun onRecovered(view: View) {
        adapter.stopLoading(presenter.getSaved())
    }

    private fun initAdapter() {
        adapter = UsersAdapter(safeActivity, { loadMore(it) }, {
            val user = adapter.items[it]
            hideKeyboard(safeActivity)
            rootActivity.loadFragment(ProfileFragment.newInstance(user.id))
        })
        rvUsers.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
        rvUsers.adapter = adapter
    }

    private fun initSearchView() {
        searchView.setBackgroundColor(resources.getColor(R.color.popup))
        searchView.setTextColor(resources.getColor(R.color.main_text))
        searchView.setHintTextColor(resources.getColor(R.color.other_text))
        searchView.showSearch()
        searchView.setOnQueryTextListener(object : MaterialSearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                presenter.searchUsers(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (timer != null) {
                    timer!!.cancel()
                    timer = null
                }
                timer = object : CountDownTimer(300L, 300L) {
                    override fun onTick(l: Long) {}

                    override fun onFinish() {
                        onQueryTextSubmit(newText)
                    }
                }.start()
                return true
            }
        })
        searchView.setOnSearchViewListener(object : MaterialSearchView.SearchViewListener {
            override fun onSearchViewClosed() {
                rootActivity.onBackPressed()
            }

            override fun onSearchViewShown() {}
        })
        searchView.clearFocus()
//        searchView.post {
//            searchView.showSearch()
//            searchView.showKeyboard(searchView)
//        }
    }

    private fun loadMore(offset: Int = 0) {
        presenter.searchUsers(offset = offset)
    }

    override fun showLoading() {
        adapter.startLoading()
    }

    override fun hideLoading() {
    }

    override fun showError(error: String) {
        adapter.setErrorLoading()
    }

    override fun onUsersClear() {
        adapter.clear()
    }

    override fun onUsersLoaded(users: MutableList<User>) {
        CacheHelper.saveUsersAsync(users)
        adapter.stopLoading(users)
    }

    override fun getLayout() = R.layout.fragment_users_search
}