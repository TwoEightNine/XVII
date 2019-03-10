package com.twoeightnine.root.xvii.friends.fragments

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import butterknife.BindView
import butterknife.ButterKnife
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.fragments.BaseFragment
import com.twoeightnine.root.xvii.friends.adapters.UsersAdapter
import com.twoeightnine.root.xvii.profile.fragments.ProfileFragment

class FriendsOnlineFragment: BaseFragment() {

    companion object {
        fun newInstance(loadMore: (Int) -> Unit): FriendsOnlineFragment {
            val frag = FriendsOnlineFragment()
            frag.loadMore = loadMore
            return frag
        }
    }

    @BindView(R.id.rvUsers)
    lateinit var rvUsers: androidx.recyclerview.widget.RecyclerView

    lateinit var adapter: UsersAdapter

    var loadMore: ((Int) -> Unit)? = null

    override fun bindViews(view: View) {
        super.bindViews(view)
        ButterKnife.bind(this, view)
        initAdapter()
    }

    private fun initAdapter() {
        adapter = UsersAdapter(safeActivity, { loadMore?.invoke(it) }, {
            val user = adapter.items[it]
            rootActivity.loadFragment(ProfileFragment.newInstance(user.id))
        })
        rvUsers.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
        rvUsers.adapter = adapter
    }

    override fun getLayout() = R.layout.fragment_friends_simple
}