package com.twoeightnine.root.xvii.friends.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.friends.adapters.FriendsAdapter
import com.twoeightnine.root.xvii.friends.viewmodel.FriendsViewModel
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.profile.fragments.ProfileFragment
import com.twoeightnine.root.xvii.utils.showError
import kotlinx.android.synthetic.main.fragment_friends.*
import javax.inject.Inject

class FriendsFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: FriendsViewModel.Factory
    private lateinit var viewModel: FriendsViewModel

    private val adapter by lazy { FriendsAdapter(contextOrThrow, ::onClick) }

    override fun getLayoutId() = R.layout.fragment_friends

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        App.appComponent?.inject(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory)[FriendsViewModel::class.java]
        viewModel.getFriends().observe(this, Observer { updateFriends(it) })
        viewModel.loadFriends()

        rvFriends.layoutManager = LinearLayoutManager(context)
        rvFriends.adapter = adapter
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setTitle(getString(R.string.fiends))
    }

    private fun updateFriends(data: Wrapper<ArrayList<User>>) {
        if (data.data != null) {
            adapter.update(data.data)
        } else {
            showError(context, data.error ?: getString(R.string.error))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.clear()
        inflater?.inflate(R.menu.user_list_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId){
            R.id.menu_search -> {
                rootActivity?.loadFragment(SearchUsersFragment())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onClick(user: User) {
        rootActivity?.loadFragment(ProfileFragment.newInstance(user.id))
    }

    companion object {
        fun newInstance() = FriendsFragment()
    }
}