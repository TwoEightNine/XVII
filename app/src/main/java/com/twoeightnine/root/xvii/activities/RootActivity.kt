package com.twoeightnine.root.xvii.activities

//import com.twoeightnine.root.xvii.dialogs.fragments.DialogsFragment
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.DrawerAdapter
import com.twoeightnine.root.xvii.background.longpoll.LongPollCore
import com.twoeightnine.root.xvii.chats.fragments.ChatFragment
import com.twoeightnine.root.xvii.dialogs.fragments.DialogsFragment
import com.twoeightnine.root.xvii.features.FeaturesFragment
import com.twoeightnine.root.xvii.fragments.BaseOldFragment
import com.twoeightnine.root.xvii.friends.fragments.FriendsFragment
import com.twoeightnine.root.xvii.lg.Lg
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.managers.Session
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.model.DrawerItem
import com.twoeightnine.root.xvii.profile.fragments.ProfileFragment
import com.twoeightnine.root.xvii.utils.*
import kotlinx.android.synthetic.main.activity_root.*
import javax.inject.Inject

/**
 * this is where all fragments are placed
 */
class RootActivity : BaseActivity() {

    companion object {

        const val USER_ID = "userId"
        const val TITLE = "title"

        const val DIALOGS: String = "DialogsFragment"
        const val FRIENDS: String = "FriendsFragment"
        const val SETTINGS: String = "SettingsFragment"
        const val FEATURES: String = "FeaturesFragment"
        const val PROFILE: String = "ProfileFragment"
        const val CHAT: String = "ChatFragment"

    }

    @Inject
    lateinit var apiUtils: ApiUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_root)
        App.appComponent?.inject(this)
        initDrawer()
        loadFragment(DialogsFragment.newInstance())
        intent?.extras?.apply {
            val userId = getInt(USER_ID)
            if (userId != 0) {
                val title = getString(TITLE) ?: ""
                loadFragment(ChatFragment.newInstance(userId, title))
                Lg.i("open chat $userId")
            }
        }
        styleScreen(flContainer)
        startNotificationAlarm(this)
        apiUtils.trackVisitor()
//        StatTool.get()?.incLaunch()
    }

    private fun initDrawer() {
        val toggle = ActionBarDrawerToggle(
                this, dlRoot, R.string.about_app, R.string.about_app)
        dlRoot.setDrawerListener(toggle)
        toggle.syncState()
        val adapter = DrawerAdapter()
        adapter.add(DrawerItem(getString(R.string.dialogs), R.drawable.ic_dialogs))
        adapter.add(DrawerItem(getString(R.string.fiends), R.drawable.ic_friends))
        adapter.add(DrawerItem(getString(R.string.settings), R.drawable.ic_settings))
        lvDrawer.adapter = adapter
        lvDrawer.setOnItemClickListener { _, _, i, _ ->
            val item = adapter.getItem(i)
            when (item.resId) {
                R.drawable.ic_friends -> loadFragment(FriendsFragment.newInstance(), true)
                R.drawable.ic_settings -> loadFragment(FeaturesFragment.newInstance(), true)
                R.drawable.ic_dialogs -> loadFragment(DialogsFragment.newInstance(), true)
            }
            hideKeyboard(this)
            dlRoot.closeDrawer(GravityCompat.START)
        }
        if (Prefs.isLightTheme) {
            navigationView.setBackgroundColor(Style.getFromMain()[1])
        }
        initUser()
    }

    private fun initUser() {
        tvFullName.text = Session.fullName
        civAvatar.load(Session.photo)
        civAvatar.setOnClickListener {
            loadFragment(ProfileFragment.newInstance(Session.uid))
            hideKeyboard(this)
            dlRoot.closeDrawer(GravityCompat.START)
        }
    }

    /**
     * different logic for keeping DialogFragment always in the back stack
     * + it prevents recreating top fragment if it is called
     * + it removes top fragment (if fragment is not Dialogs)
     */
    fun loadFragment(frag: androidx.fragment.app.Fragment, clearStack: Boolean = false, tag: String = frag.javaClass.simpleName) {
        val count = supportFragmentManager.backStackEntryCount
        if (count > 0) {
            val topName = supportFragmentManager.getBackStackEntryAt(count - 1).name
            if (topName == tag &&
                    (tag == FRIENDS ||
                            tag == SETTINGS ||
                            tag == FEATURES ||
                            tag == DIALOGS)) {
                return
            }

            if (clearStack && supportFragmentManager.backStackEntryCount > 1) {
                for (i in 1 until supportFragmentManager.backStackEntryCount) {
                    supportFragmentManager.popBackStackImmediate()
                }
            }

            val found = supportFragmentManager.findFragmentByTag(tag)
            if (found != null) {
                supportFragmentManager
                        .beginTransaction()
                        .remove(found)
                        .commit()

                if (tag != CHAT && tag != PROFILE) {
                    supportFragmentManager
                            .beginTransaction()
                            .add(R.id.flContainer, found, tag)
                            .commit()
                    return
                }
            }

        }
        try {
            supportFragmentManager
                    .beginTransaction()
                    .add(R.id.flContainer, frag, tag)
                    .addToBackStack(tag)
                    .commit()
        } catch (e: IllegalStateException) {
            showError(this, e.message ?: "Illegal state exception")
            restartApp()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!LongPollCore.isRunning()) {
            Lg.i("Service wasn't active since " +
                    "${getTime(LongPollCore.lastRun, format = "HH:mm:ss")}. Start again")
            Handler().postDelayed({ startNotificationService(this) }, 1000L)
        }
        removeNotification(this)
    }

    /**
     * priority:
     *  - close drawer
     *  - remove fragment until dialogs
     *  - go home (never actually close)
     */
    override fun onBackPressed() {
        if (dlRoot.isDrawerOpen(GravityCompat.START)) {
            dlRoot.closeDrawer(GravityCompat.START)
        } else if (supportFragmentManager.backStackEntryCount > 0) {
            val topFrag = try {
                supportFragmentManager.fragments[supportFragmentManager.backStackEntryCount - 1]
            } catch (e: Exception) {
                Lg.wtf("onBackPressed: ${e.message}")
                e.printStackTrace()
                null
            }
            if (topFrag is BaseOldFragment && topFrag.onBackPressed()) {
                return
            } else if (supportFragmentManager.backStackEntryCount > 1) {
                supportFragmentManager.popBackStack()
            } else {
                val intent = Intent(Intent.ACTION_MAIN)
                intent.addCategory(Intent.CATEGORY_HOME)
                startActivity(intent)
            }
        } else {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                dlRoot.openDrawer(GravityCompat.START)
                true
            }
            else -> false
        }
    }
}
