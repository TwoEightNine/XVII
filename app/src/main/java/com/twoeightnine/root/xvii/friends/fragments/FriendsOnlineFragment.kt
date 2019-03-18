package com.twoeightnine.root.xvii.friends.fragments

import android.view.View
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.fragments.BaseOldFragment
import com.twoeightnine.root.xvii.friends.adapters.UsersAdapter
import com.twoeightnine.root.xvii.profile.fragments.ProfileFragment
import kotlinx.android.synthetic.main.fragment_friends_simple.*

class FriendsOnlineFragment: BaseOldFragment() {

    companion object {
        fun newInstance(loadMore: (Int) -> Unit): FriendsOnlineFragment {
            val frag = FriendsOnlineFragment()
            frag.loadMore = loadMore
            return frag
        }
    }

    lateinit var adapter: UsersAdapter

    var loadMore: ((Int) -> Unit)? = null

    override fun bindViews(view: View) {
        super.bindViews(view)
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