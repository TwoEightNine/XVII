package com.twoeightnine.root.xvii.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.DrawerAdapter
import com.twoeightnine.root.xvii.background.LongPollService
import com.twoeightnine.root.xvii.background.MediaPlayerAsyncTask
import com.twoeightnine.root.xvii.chats.fragments.ChatFragment
import com.twoeightnine.root.xvii.dialogs.fragments.DialogsFragment
import com.twoeightnine.root.xvii.fragments.BaseFragment
import com.twoeightnine.root.xvii.friends.fragments.FriendsFragment
import com.twoeightnine.root.xvii.managers.Lg
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.managers.Session
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.model.DrawerItem
import com.twoeightnine.root.xvii.model.Message
import com.twoeightnine.root.xvii.profile.fragments.ProfileFragment
import com.twoeightnine.root.xvii.settings.fragments.SettingsFragment
import com.twoeightnine.root.xvii.utils.*
import de.hdodenhof.circleimageview.CircleImageView

/**
 * this is where all fragments are placed
 */
class RootActivity : BaseActivity() {

    companion object {

        var player: MediaPlayerAsyncTask? = null

        val USER_ID = "userId"
        val TITLE = "title"

        val DIALOGS: String = "DialogsFragment"
        val FRIENDS: String = "FriendsFragment"
        val SETTINGS: String = "SettingsFragment"
        val PROFILE: String = "ProfileFragment"
        val CHAT: String = "ChatFragment"

    }


    @BindView(R.id.flContainer)
    lateinit var flContainer: FrameLayout
    @BindView(R.id.dlRoot)
    lateinit var dlRoot: DrawerLayout
    @BindView(R.id.navigationView)
    lateinit var navigationView: NavigationView
    @BindView(R.id.llDrawer)
    lateinit var llDrawer: LinearLayout
    @BindView(R.id.civAvatar)
    lateinit var civAvatar: CircleImageView
    @BindView(R.id.tvFullName)
    lateinit var tvFullName: TextView
    @BindView(R.id.lvDrawer)
    lateinit var lvDrawer: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_root)
        ButterKnife.bind(this)
        initDrawer()
        loadFragment(DialogsFragment.newInstance())
        if (intent.extras != null && intent.extras.getInt(USER_ID) != 0) {
            val userId = intent.extras.getInt(USER_ID)
            val title = intent.extras.getString(TITLE)
            val message = Message(
                    0, 0, userId, 0, 0, title, "", null
            )
            loadFragment(ChatFragment.newInstance(message))
            Lg.i("open chat ${intent.extras.getInt(USER_ID)}")
        }
        styleScreen(flContainer)
        Handler().postDelayed({ startService(Intent(this, LongPollService::class.java)) }, 5000L)
        removeNotification(this)
        pushUser()
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
                R.drawable.ic_settings -> loadFragment(SettingsFragment.newInstance(), true)
                R.drawable.ic_dialogs -> loadFragment(DialogsFragment.newInstance(), true)
            }
            hideKeyboard(this)
            dlRoot.closeDrawer(GravityCompat.START)
        }
        if (Prefs.isNight) {
            navigationView.setBackgroundColor(Style.getFromMain()[1])
        }
        initUser()
    }

    private fun initUser() {
        tvFullName.text = Session.fullName
        civAvatar.loadPhoto(Session.photo)
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
    fun loadFragment(frag: Fragment, clearStack: Boolean = false, tag: String = frag.javaClass.simpleName) {
        val count = supportFragmentManager.backStackEntryCount
        if (count > 0) {
            val topName = supportFragmentManager.getBackStackEntryAt(count - 1).name
            if (topName == tag &&
                    (tag == FRIENDS ||
                            tag == SETTINGS ||
                            tag == DIALOGS)) {
                return
            }

            if (clearStack && supportFragmentManager.backStackEntryCount > 1) {
                for(i in 1 until supportFragmentManager.backStackEntryCount) {
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
        if (!Session.isActive()) {
            Lg.i("Service wasn't active since ${getTime(Session.serviceLastAction, true, "HH:mm:ss")}. Start again")
            Handler().postDelayed({ startService(Intent(this, LongPollService::class.java)) }, 5000L)
        }
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
            val topFrag = supportFragmentManager.fragments[supportFragmentManager.backStackEntryCount - 1]
            if (topFrag is BaseFragment && topFrag.onBackPressed()) {
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