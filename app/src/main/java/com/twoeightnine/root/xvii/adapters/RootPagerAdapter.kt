package com.twoeightnine.root.xvii.adapters

import com.twoeightnine.root.xvii.dialogs.fragments.DialogsFragment
import com.twoeightnine.root.xvii.friends.fragments.FriendsFragment
import com.twoeightnine.root.xvii.friends.fragments.SearchUsersFragment
import com.twoeightnine.root.xvii.settings.fragments.SettingsFragment

class RootPagerAdapter(fm : androidx.fragment.app.FragmentManager) : CommonPagerAdapter(fm) {

    init {
        add(DialogsFragment.newInstance(), "Chats")
        add(FriendsFragment.newInstance(), "Friends")
        add(SettingsFragment.newInstance(), "Settings")
        add(SearchUsersFragment(), "Search")
    }

}