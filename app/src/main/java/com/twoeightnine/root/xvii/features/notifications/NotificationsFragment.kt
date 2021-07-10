/*
 * xvii - messenger for vk
 * Copyright (C) 2021  TwoEightNine
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.twoeightnine.root.xvii.features.notifications

import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.CompoundButton
import com.twoeightnine.root.xvii.BuildConfig
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.base.FragmentPlacementActivity.Companion.startFragment
import com.twoeightnine.root.xvii.egg.EggFragment
import com.twoeightnine.root.xvii.features.notifications.color.ColorAlertDialog
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.utils.NotificationChannels
import global.msnthrp.xvii.uikit.extensions.applyBottomInsetPadding
import global.msnthrp.xvii.uikit.extensions.hide
import global.msnthrp.xvii.uikit.extensions.show
import kotlinx.android.synthetic.main.fragment_notifications.*

/**
 * Created by root on 2/2/17.
 */

class NotificationsFragment : BaseFragment() {

    private var eggState = 0

    override fun getLayoutId() = R.layout.fragment_notifications

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
            switchContentChats.isChecked = Prefs.showContentChats
        } else {
            switchNotifsChats.isChecked = false
            switchVibrateChats.isEnabled = false
            switchSoundChats.isEnabled = false
            switchContentChats.isEnabled = false
        }
        switchStylizeNotifications.isChecked = Prefs.useStyledNotifications
        switchShowNotification.onCheckedListener = CompoundButton.OnCheckedChangeListener { _, b ->
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
        switchNotifsChats.onCheckedListener = CompoundButton.OnCheckedChangeListener { _, b ->
            switchVibrateChats.isEnabled = b
            switchSoundChats.isEnabled = b
            switchContentChats.isEnabled = b
            if (!b) {
                switchVibrateChats.isChecked = false
                switchSoundChats.isChecked = false
                switchContentChats.isChecked = false
            }
        }
        csLed.setOnClickListener {
            context?.also {
                ColorAlertDialog(it) { color ->
                    Prefs.ledColor = color
                    csLed.color = color
                }.show()
            }
        }
        csLedChats.setOnClickListener {
            context?.also {
                ColorAlertDialog(it) { color ->
                    Prefs.ledColorChats = color
                    csLedChats.color = color
                }.show()
            }
        }
        csLed.color = Prefs.ledColor
        csLedChats.color = Prefs.ledColorChats
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        svContent.applyBottomInsetPadding()
    }

    private fun saveSwitches() {
        Prefs.showNotifs = switchShowNotification.isChecked
        Prefs.showName = switchShowName.isEnabled && switchShowName.isChecked
        Prefs.vibrate = switchVibrate.isEnabled && switchVibrate.isChecked
        Prefs.sound = switchSound.isEnabled && switchSound.isChecked
        Prefs.showContent = switchContent.isEnabled && switchContent.isChecked

        Prefs.showNotifsChats = switchNotifsChats.isChecked
        Prefs.vibrateChats = switchVibrateChats.isEnabled && switchVibrateChats.isChecked
        Prefs.soundChats = switchSoundChats.isEnabled && switchSoundChats.isChecked
        Prefs.showContentChats = switchContentChats.isEnabled && switchContentChats.isChecked

        Prefs.useStyledNotifications = switchStylizeNotifications.isChecked
    }

    override fun onStop() {
        super.onStop()
        saveSwitches()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSwitches()
        initEgg()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            listOf(switchVibrate, switchSound, csLed,
                    switchVibrateChats, switchSoundChats, csLedChats)
                    .forEach { it.hide() }
            listOf(btnSettingsPrivate, btnSettings)
                    .forEach { it.show() }
            btnSettingsPrivate.setOnClickListener {
                openChannelSettings(NotificationChannels.privateMessages.id)
            }
            btnSettingsOther.setOnClickListener {
                openChannelSettings(NotificationChannels.otherMessages.id)
            }
            btnSettings.setOnClickListener {
                openChannelSettings()
            }
        } else {
            listOf(btnSettingsPrivate, btnSettingsOther)
                    .forEach { it.hide() }
            btnSettings.setOnClickListener {
                openSettingsPreOreo()
            }
        }
    }

    private fun initEgg() {
        if (Math.random() > 0.8 || BuildConfig.DEBUG) {
            val handler = Handler(Looper.getMainLooper())
            switchEgg.show()
            switchEgg.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    when (eggState) {
                        1 -> {
                            eggState = -1
                            handler.postDelayed({ switchEgg.isChecked = false }, 500L)
                            handler.postDelayed({
                                startFragment<EggFragment>()
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

    @TargetApi(26)
    private fun openChannelSettings(channelId: String? = null) {
        val action = if (channelId == null) {
            Settings.ACTION_APP_NOTIFICATION_SETTINGS
        } else {
            Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS
        }
        startActivity(Intent(action).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context?.packageName)
            channelId?.also {
                putExtra(Settings.EXTRA_CHANNEL_ID, channelId)
            }
        })
    }

    private fun openSettingsPreOreo() {
        startActivity(Intent().apply {
            action = "android.settings.APP_NOTIFICATION_SETTINGS"
            putExtra("app_package", context?.packageName)
            putExtra("app_uid", context?.applicationInfo?.uid)
        })
    }

    companion object {
        fun newInstance() = NotificationsFragment()
    }
}
