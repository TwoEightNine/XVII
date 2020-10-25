package com.twoeightnine.root.xvii.main

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.ads.MobileAds
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseActivity
import com.twoeightnine.root.xvii.chats.messages.chat.usual.ChatActivity
import com.twoeightnine.root.xvii.lg.L
import com.twoeightnine.root.xvii.utils.*
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : BaseActivity() {

    @Inject
    lateinit var apiUtils: ApiUtils

    private val insetViewModel by lazy {
        ViewModelProviders.of(this)[InsetViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        App.appComponent?.inject(this)
        bottomNavView.setOnNavigationItemSelectedListener(BottomViewListener())
        initViewPager()
        bottomNavView.selectedItemId = R.id.menu_dialogs
        bottomNavView.setBottomInsetPadding(resources.getDimensionPixelSize(R.dimen.bottom_navigation_height))
        vStubConsumer.consumeInsets { top, bottom ->
            insetViewModel.updateTopInset(top)
            insetViewModel.updateBottomInset(bottom)
        }

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
        stylize(isWhite = true)
        bottomNavView.stylize()
        StatTool.get()?.incLaunch()
        MobileAds.initialize(this) {}
    }

    private fun initViewPager() {
        with(viewPager) {
            adapter = MainPagerAdapter(supportFragmentManager)
            isLocked = true
            offscreenPageLimit = 2
        }
    }

    override fun getStatusBarColor() = ContextCompat.getColor(this, R.color.status_bar)

    override fun getNavigationBarColor() = Color.TRANSPARENT

    override fun onResume() {
        super.onResume()
        stylize(isWhite = true)
    }

    override fun getThemeId() = R.style.AppTheme_Main

    override fun onBackPressed() {
        if (viewPager.currentItem == 1) {
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
            viewPager.setCurrentItem(
                    when (item.itemId) {
//                        R.id.menu_search -> 0
                        R.id.menu_friends -> 0
                        R.id.menu_features -> 2
                        else -> 1 // default menu_dialogs
                    },
                    false
            )
            return true
        }

    }
}