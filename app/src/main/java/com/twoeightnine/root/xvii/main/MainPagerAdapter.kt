package com.twoeightnine.root.xvii.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.twoeightnine.root.xvii.dialogs.fragments.DialogsFragment
import com.twoeightnine.root.xvii.features.FeaturesFragment
import com.twoeightnine.root.xvii.friends.fragments.FriendsFragment
import com.twoeightnine.root.xvii.search.SearchFragment

class MainPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

    private val fragments = arrayListOf<Fragment>()

    init {
        fragments.apply {
            add(SearchFragment.newInstance())
            add(DialogsFragment.newInstance())
            add(FriendsFragment.newInstance())
            add(FeaturesFragment.newInstance())
        }
    }

    override fun getCount() = fragments.size

    override fun getItem(position: Int) = fragments[position]
}