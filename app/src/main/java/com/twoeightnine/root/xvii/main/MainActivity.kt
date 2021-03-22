package com.twoeightnine.root.xvii.main

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseActivity
import com.twoeightnine.root.xvii.base.FragmentPlacementActivity.Companion.startFragment
import com.twoeightnine.root.xvii.chats.messages.chat.usual.ChatActivity
import com.twoeightnine.root.xvii.dialogs.fragments.DialogsFragment
import com.twoeightnine.root.xvii.features.FeaturesFragment
import com.twoeightnine.root.xvii.friends.fragments.FriendsFragment
import com.twoeightnine.root.xvii.lg.L
import com.twoeightnine.root.xvii.search.SearchFragment
import com.twoeightnine.root.xvii.uikit.Munch
import com.twoeightnine.root.xvii.uikit.paint
import com.twoeightnine.root.xvii.utils.*
import global.msnthrp.xvii.uikit.extensions.applyTopInsetMargin
import global.msnthrp.xvii.uikit.extensions.isVisible
import global.msnthrp.xvii.uikit.extensions.setVisible
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : BaseActivity() {

    @Inject
    lateinit var apiUtils: ApiUtils

    private val bottomNavViewHeight by lazy {
        resources.getDimensionPixelSize(R.dimen.bottom_navigation_height)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        App.appComponent?.inject(this)
        initFragments()
        bottomNavView.setOnNavigationItemSelectedListener(BottomViewListener())
        bottomNavView.selectedItemId = R.id.menu_dialogs

        intent?.extras?.apply {
            val userId = getInt(USER_ID)
            if (userId != 0) {
                val title = getString(TITLE) ?: ""
                val photo = getString(PHOTO)
                ChatActivity.launch(this@MainActivity, userId, title, photo)
                L.def().log("open chat $userId")
            }
        }
        startNotificationAlarm(this)
        apiUtils.trackVisitor()
        bottomNavView.paint(Munch.color.color)
        StatTool.get()?.incLaunch()

        ivSearch.setOnClickListener {
            startFragment<SearchFragment>()
        }
        ivSearch.paint(Munch.color.color)
        ivSearch.applyTopInsetMargin()

        ViewCompat.setOnApplyWindowInsetsListener(bottomNavView) { view, insets ->
            view.updatePadding(bottom = insets.systemWindowInsetBottom)
            view.layoutParams.apply {
                height = bottomNavViewHeight + insets.systemWindowInsetBottom
                view.layoutParams = this
            }
            insets
        }
    }

    private fun initFragments() {
        supportFragmentManager.beginTransaction()
                .replace(R.id.flFriends, FriendsFragment.newInstance())
                .replace(R.id.flDialogs, DialogsFragment.newInstance())
                .replace(R.id.flFeatures, FeaturesFragment.newInstance())
                .commit()
    }

    private fun showFragment(menuId: Int) {
        flFriends.setVisible(menuId == R.id.menu_friends)
        flDialogs.setVisible(menuId == R.id.menu_dialogs)
        flFeatures.setVisible(menuId == R.id.menu_features)
        ivSearch.setVisible(menuId != R.id.menu_features)
    }

    override fun getStatusBarColor() = ContextCompat.getColor(this, R.color.status_bar)

    override fun getNavigationBarColor() = Color.TRANSPARENT

    override fun getThemeId() = R.style.AppTheme_Main

    override fun onBackPressed() {
        if (flDialogs.isVisible()) {
            try {
                goHome(this)
            } catch (e: IllegalStateException) {
                L.def().throwable(e).log("unable to go home")
                super.onBackPressed()
            }
        } else {
            bottomNavView.selectedItemId = R.id.menu_dialogs
        }
    }

    companion object {

        const val USER_ID = "userId"
        const val TITLE = "title"
        const val PHOTO = "photo"

        fun launch(context: Context?) {
            launchActivity(context, MainActivity::class.java)
        }
    }

    private inner class BottomViewListener : BottomNavigationView.OnNavigationItemSelectedListener {
        override fun onNavigationItemSelected(item: MenuItem): Boolean {
            showFragment(item.itemId)
            return true
        }

    }
}