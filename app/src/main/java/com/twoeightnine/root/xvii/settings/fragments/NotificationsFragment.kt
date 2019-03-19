package com.twoeightnine.root.xvii.settings.fragments

import android.os.Bundle
import android.os.Handler
import android.view.View
import com.twoeightnine.root.xvii.BuildConfig
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.fragments.BaseOldFragment
import com.twoeightnine.root.xvii.fragments.EggFragment
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.managers.Style
import kotlinx.android.synthetic.main.fragment_notifications.*

/**
 * Created by root on 2/2/17.
 */

class NotificationsFragment : BaseOldFragment() {

    private var eggState = 0

    private fun initSwitches() {
        if (Prefs.showNotifs) {
            switchShowNotification.isChecked = true
            switchShowName.isChecked = Prefs.showName
            switchChats.isChecked = Prefs.showNotifsChats
            switchVibrate.isChecked = Prefs.vibrate
            switchSound.isChecked = Prefs.sound
            switchLights.isChecked = Prefs.ledLights
            switchContent.isChecked = Prefs.showContent
        } else {
            switchShowNotification.isChecked = false
            switchShowName.isEnabled = false
            switchChats.isEnabled = false
            switchVibrate.isEnabled = false
            switchSound.isEnabled = false
            switchLights.isEnabled = false
            switchContent.isEnabled = false
        }
        switchShowNotification.setOnCheckedChangeListener { _, b ->
            switchShowName.isEnabled = b
            switchVibrate.isEnabled = b
            switchSound.isEnabled = b
            switchChats.isEnabled = b
            switchLights.isEnabled = b
            switchContent.isEnabled = b
            if (!b) {
                switchShowName.isChecked = false
                switchVibrate.isChecked = false
                switchSound.isChecked = false
                switchChats.isChecked = false
                switchLights.isChecked = false
                switchContent.isChecked = false
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        updateTitle(getString(R.string.notifications))
    }

    private fun saveSwitches() {
        Prefs.showNotifs = switchShowNotification.isChecked
        Prefs.showNotifsChats = switchChats.isChecked
        Prefs.showName = switchShowName.isEnabled && switchShowName.isChecked
        Prefs.vibrate = switchVibrate.isEnabled && switchVibrate.isChecked
        Prefs.sound = switchSound.isEnabled && switchSound.isChecked
        Prefs.ledLights = switchLights.isEnabled && switchLights.isChecked
        Prefs.showContent = switchContent.isEnabled && switchContent.isChecked
    }

    override fun onStop() {
        super.onStop()
        saveSwitches()
    }

    override fun bindViews(view: View) {
        initSwitches()
        Style.forAll(llContainer)
        if (Math.random() > 0.85 || BuildConfig.DEBUG) {
            val handler = Handler()
            switchEgg.visibility = View.VISIBLE
            switchEgg.setOnCheckedChangeListener {
                _, isChecked ->
                if (isChecked) {
                    when (eggState) {
                        1 -> {
                            eggState = -2
                            handler.postDelayed({ switchEgg.isChecked = false }, 500L)
                            handler.postDelayed({ rootActivity.loadFragment(EggFragment.newInstance(EggFragment.MODE_LETOV_AGAINST)) }, 1000L)
                        }
                        else -> {
                            eggState++
                            handler.postDelayed({ switchEgg.isChecked = false }, 500L)
                        }
                    }
                }
            }
        }
    }

    override fun getLayout() = R.layout.fragment_notifications
}
