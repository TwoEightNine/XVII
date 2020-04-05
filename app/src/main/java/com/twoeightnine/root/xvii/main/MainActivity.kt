package com.twoeightnine.root.xvii.main

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.activities.BaseActivity
import com.twoeightnine.root.xvii.background.longpoll.LongPollCore
import com.twoeightnine.root.xvii.chats.messages.chat.usual.ChatActivity
import com.twoeightnine.root.xvii.lg.Lg
import com.twoeightnine.root.xvii.utils.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_root.*
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
                Lg.i("open chat $userId")
            }
        }
        startNotificationAlarm(this)
        apiUtils.trackVisitor()
        stylize(isWhite = true)
        bottomNavView.stylize()
        StatTool.get()?.incLaunch()
    }

    private fun initViewPager() {
        with(viewPager) {
            adapter = MainPagerAdapter(supportFragmentManager)
            isLocked = true
            offscreenPageLimit = 3
        }
    }

    override fun getStatusBarColor() = ContextCompat.getColor(this, R.color.status_bar)

    override fun getNavigationBarColor() = Color.TRANSPARENT

    override fun onResume() {
        super.onResume()
        stylize(isWhite = true)
        if (!LongPollCore.isRunning()) {
            Lg.i("service wasn't active since " +
                    "${getTime(LongPollCore.lastRun, withSeconds = true)}. start again")
            Handler().postDelayed({ startNotificationService(this) }, 1000L)
        }
//        removeNotification(this)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                dlRoot.openDrawer(GravityCompat.START)
                true
            }
            else -> false
        }
    }

    override fun getThemeId() = R.style.AppTheme_Main

    override fun onBackPressed() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        startActivity(intent)
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
                        R.id.menu_search -> 0
                        R.id.menu_friends -> 2
                        R.id.menu_features -> 3
                        else -> 1 // default menu_dialogs
                    },
                    false
            )
            return true
        }

    }
}