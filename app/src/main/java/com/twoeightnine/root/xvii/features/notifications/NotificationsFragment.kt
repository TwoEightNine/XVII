package com.twoeightnine.root.xvii.features.notifications

import android.os.Bundle
import android.os.Handler
import android.view.View
import com.twoeightnine.root.xvii.BuildConfig
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.egg.EggActivity
import com.twoeightnine.root.xvii.egg.EggFragment
import com.twoeightnine.root.xvii.features.notifications.color.ColorAlertDialog
import com.twoeightnine.root.xvii.fragments.BaseOldFragment
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.managers.Style
import kotlinx.android.synthetic.main.fragment_notifications.*

/**
 * Created by root on 2/2/17.
 */

class NotificationsFragment : BaseOldFragment() {

    private var eggState = 0

    override fun getLayout() = R.layout.fragment_notifications

    private fun initSwitches() {
        if (Prefs.showNotifs) {
            switchShowNotification.isChecked = true
            switchShowName.isChecked = Prefs.showName
            switchVibrate.isChecked = Prefs.vibrate
            switchSound.isChecked = Prefs.sound
            switchContent.isChecked = Prefs.showContent
        } else {
            switchShowNotification.isChecked = false
            switchShowName.isEnabled = false
            switchVibrate.isEnabled = false
            switchSound.isEnabled = false
            switchContent.isEnabled = false
        }
        if (Prefs.showNotifsChats) {
            switchNotifsChats.isChecked = true
            switchVibrateChats.isChecked = Prefs.vibrateChats
            switchSoundChats.isChecked = Prefs.soundChats
            switchLightsChats.isChecked = Prefs.ledLightsChats
            switchContentChats.isChecked = Prefs.showContentChats
        } else {
            switchNotifsChats.isChecked = false
            switchVibrateChats.isEnabled = false
            switchSoundChats.isEnabled = false
            switchLightsChats.isEnabled = false
            switchContentChats.isEnabled = false
        }
        switchShowNotification.setOnCheckedChangeListener { _, b ->
            switchShowName.isEnabled = b
            switchVibrate.isEnabled = b
            switchSound.isEnabled = b
            switchContent.isEnabled = b
            if (!b) {
                switchShowName.isChecked = false
                switchVibrate.isChecked = false
                switchSound.isChecked = false
                switchContent.isChecked = false
            }
        }
        switchNotifsChats.setOnCheckedChangeListener { _, b ->
            switchVibrateChats.isEnabled = b
            switchSoundChats.isEnabled = b
            switchLightsChats.isEnabled = b
            switchContentChats.isEnabled = b
            if (!b) {
                switchVibrateChats.isChecked = false
                switchSoundChats.isChecked = false
                switchLightsChats.isChecked = false
                switchContentChats.isChecked = false
            }
        }
        rlLedColor.setOnClickListener {
            ColorAlertDialog(safeContext) { color ->
                Prefs.ledColor = color
                ivLedColor.setBackgroundColor(color)
            }.show()
        }
        rlLedColorChats.setOnClickListener {
            ColorAlertDialog(safeContext) { color ->
                Prefs.ledColorChats = color
                ivLedColorChats.setBackgroundColor(color)
            }.show()
        }
        ivLedColor.setBackgroundColor(Prefs.ledColor)
        ivLedColorChats.setBackgroundColor(Prefs.ledColorChats)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        updateTitle(getString(R.string.notifications))
    }

    override fun getHomeAsUpIcon() = R.drawable.ic_back

    private fun saveSwitches() {
        Prefs.showNotifs = switchShowNotification.isChecked
        Prefs.showName = switchShowName.isEnabled && switchShowName.isChecked
        Prefs.vibrate = switchVibrate.isEnabled && switchVibrate.isChecked
        Prefs.sound = switchSound.isEnabled && switchSound.isChecked
        Prefs.showContent = switchContent.isEnabled && switchContent.isChecked

        Prefs.showNotifsChats = switchNotifsChats.isChecked
        Prefs.vibrateChats = switchVibrateChats.isEnabled && switchVibrateChats.isChecked
        Prefs.soundChats = switchSoundChats.isEnabled && switchSoundChats.isChecked
        Prefs.ledLightsChats = switchLightsChats.isEnabled && switchLightsChats.isChecked
        Prefs.showContentChats = switchContentChats.isEnabled && switchContentChats.isChecked
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
            switchEgg.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    when (eggState) {
                        1 -> {
                            eggState = -2
                            handler.postDelayed({ switchEgg.isChecked = false }, 500L)
                            handler.postDelayed({
//                                rootActivity.loadFragment(EggFragment.newInstance(EggFragment.MODE_LETOV_AGAINST))
                                EggActivity.launch(context, EggFragment.MODE_LETOV_AGAINST)
                            }, 1000L)
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

    companion object {
        fun newInstance() = NotificationsFragment()
    }
}
