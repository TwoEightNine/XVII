package com.twoeightnine.root.xvii.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter

/**
 * base PagerAdapter, the most common
 */
class CommonPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

    private val fragments = ArrayList<Fragment>()
    private val titles = ArrayList<String>()

    override fun getItem(position: Int) = fragments[position]

    override fun getCount() = fragments.size

    fun add(f: Fragment, title: String) {
        fragments.add(f)
        titles.add(title)
    }

    override fun getPageTitle(position: Int) = titles[position]
}