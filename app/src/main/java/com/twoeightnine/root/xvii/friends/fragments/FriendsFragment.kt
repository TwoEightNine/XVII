package com.twoeightnine.root.xvii.friends.fragments

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.base.FragmentPlacementActivity.Companion.startFragment
import com.twoeightnine.root.xvii.chatowner.ChatOwnerActivity
import com.twoeightnine.root.xvii.friends.adapters.FriendsAdapter
import com.twoeightnine.root.xvii.friends.viewmodel.FriendsViewModel
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.search.SearchFragment
import com.twoeightnine.root.xvii.utils.AppBarLifter
import com.twoeightnine.root.xvii.utils.showError
import global.msnthrp.xvii.uikit.extensions.applyBottomInsetPadding
import global.msnthrp.xvii.uikit.extensions.hide
import global.msnthrp.xvii.uikit.extensions.show
import kotlinx.android.synthetic.main.fragment_friends.*
import javax.inject.Inject

class FriendsFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: FriendsViewModel.Factory
    private lateinit var viewModel: FriendsViewModel

    private val adapter by lazy {
        FriendsAdapter(requireContext(), ::onClick, ::loadMore)
    }

    override fun getLayoutId() = R.layout.fragment_friends

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        App.appComponent?.inject(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory)[FriendsViewModel::class.java]

        adapter.startLoading()

        progressBar.show()
        rvFriends.layoutManager = LinearLayoutManager(context)
        rvFriends.adapter = adapter
        rvFriends.addOnScrollListener(AppBarLifter(xviiToolbar))

        swipeRefresh.setOnRefreshListener {
            viewModel.loadFriends()
            adapter.reset()
            adapter.startLoading()
        }
        rvFriends.applyBottomInsetPadding()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.getFriends().observe(viewLifecycleOwner, ::updateFriends)
        viewModel.loadFriends()
    }

    override fun getMenu(): Int = R.menu.search

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.menu_search -> {
            startFragment<SearchFragment>()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun updateFriends(data: Wrapper<ArrayList<User>>) {
        swipeRefresh.isRefreshing = false
        progressBar.hide()
        if (data.data != null) {
            val d = arrayListOf<User>()
            for (i in 1..10) {
                d.addAll(data.data)
            }
            adapter.update(d)
        } else {
            showError(context, data.error ?: getString(R.string.error))
        }
    }

    private fun loadMore(offset: Int) {
        viewModel.loadFriends(offset)
    }

    private fun onClick(user: User) {
        ChatOwnerActivity.launch(context, user.id)
    }

    companion object {
        fun newInstance() = FriendsFragment()
    }
}