package com.twoeightnine.root.xvii.settings.fragments

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.LinearLayout
import android.widget.Switch
import butterknife.BindView
import butterknife.ButterKnife
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.fragments.BaseFragment
import com.twoeightnine.root.xvii.fragments.EggFragment
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.managers.Style

/**
 * Created by root on 2/2/17.
 */

class NotificationsFragment : BaseFragment() {

    @BindView(R.id.switchShowNotification)
    lateinit var showNotification: Switch
    @BindView(R.id.switchShowName)
    lateinit var showName: Switch
    @BindView(R.id.switchChats)
    lateinit var chats: Switch
    @BindView(R.id.switchVibrate)
    lateinit var vibrate: Switch
    @BindView(R.id.switchSound)
    lateinit var sound: Switch
    @BindView(R.id.switchContent)
    lateinit var content: Switch
    @BindView(R.id.llContainer)
    lateinit var llContainer: LinearLayout
    @BindView(R.id.switchEgg)
    lateinit var egg: Switch

    private var eggState = 0

    private fun initSwitches() {
        if (Prefs.showNotifs) {
            showNotification.isChecked = true
            showName.isChecked = Prefs.showName
            chats.isChecked = Prefs.showNotifsChats
            vibrate.isChecked = Prefs.vibrate
            sound.isChecked = Prefs.sound
            content.isChecked = Prefs.showContent
        } else {
            showNotification.isChecked = false
            showName.isEnabled = false
            chats.isEnabled = false
            vibrate.isEnabled = false
            sound.isEnabled = false
            content.isEnabled = false
        }
        showNotification.setOnCheckedChangeListener { _, b ->
            showName.isEnabled = b
            vibrate.isEnabled = b
            sound.isEnabled = b
            chats.isEnabled = b
            content.isEnabled = b
            if (!b) {
                showName.isChecked = false
                vibrate.isChecked = false
                sound.isChecked = false
                chats.isChecked = false
                content.isChecked = false
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        updateTitle(getString(R.string.notifications))
    }

    private fun saveSwitches() {
        Prefs.setShowNotif(showNotification.isChecked)
        Prefs.setShowNotifChats(chats.isChecked)
        Prefs.showName = showName.isEnabled && showName.isChecked;
        Prefs.vibrate = vibrate.isEnabled && vibrate.isChecked
        Prefs.sound = sound.isEnabled && sound.isChecked
        Prefs.showContent = content.isEnabled && content.isChecked
    }

    override fun onStop() {
        super.onStop()
        saveSwitches()
    }

    override fun bindViews(view: View) {
        ButterKnife.bind(this, view)
        initSwitches()
        Style.forAll(llContainer)
        if (Math.random() > 0.85) {
            val handler = Handler()
            egg.visibility = View.VISIBLE
            egg.setOnCheckedChangeListener {
                _, isChecked ->
                if (isChecked) {
                    when (eggState) {
                        1 -> {
                            eggState = -2
                            handler.postDelayed({ egg.isChecked = false }, 500L)
                            handler.postDelayed({ rootActivity.loadFragment(EggFragment()) }, 1000L)
                        }
                        else -> {
                            eggState = 1
                            handler.postDelayed({ egg.isChecked = false }, 500L)
                        }
                    }
                }
            }
        }
    }

    override fun getLayout() = R.layout.fragment_notifications
}
