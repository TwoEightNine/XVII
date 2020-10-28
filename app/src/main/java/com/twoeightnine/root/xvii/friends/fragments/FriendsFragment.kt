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
import com.twoeightnine.root.xvii.main.InsetViewModel
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.search.SearchFragment
import com.twoeightnine.root.xvii.utils.*
import kotlinx.android.synthetic.main.fragment_friends.*
import javax.inject.Inject

class FriendsFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: FriendsViewModel.Factory
    private lateinit var viewModel: FriendsViewModel

    private val adapter by lazy {
        FriendsAdapter(requireContext(), ::onClick, ::loadMore)
    }

    private val insetViewModel by lazy {
        ViewModelProviders.of(activity ?: return@lazy null)[InsetViewModel::class.java]
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
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.getFriends().observe(viewLifecycleOwner, ::updateFriends)
        viewModel.loadFriends()
//        insetViewModel?.topInset?.observe(viewLifecycleOwner) { top ->
//            adapter.firstItemPadding = top
//        }
        insetViewModel?.bottomInset?.observe(viewLifecycleOwner) { bottom ->
            val bottomNavHeight = context?.resources?.getDimensionPixelSize(R.dimen.bottom_navigation_height) ?: 0
            rvFriends.setBottomPadding(bottom + bottomNavHeight)
        }
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
            adapter.update(data.data)
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