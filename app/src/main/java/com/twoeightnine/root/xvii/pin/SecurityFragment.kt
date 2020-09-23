package com.twoeightnine.root.xvii.pin

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.CompoundButton
import androidx.core.content.ContextCompat
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.utils.*
import kotlinx.android.synthetic.main.fragment_pin_settings.*

class SecurityFragment : BaseFragment() {

    private val pinCheckedListener = OnPinChecked()

    private val minutes by lazy { getString(R.string.pin_settings_mixture_minutes) }
    private val battery by lazy { getString(R.string.pin_settings_mixture_battery) }

    private var mixtureType = MixtureType.NONE
    private var fakeAppType = FakeAppType.NONE

    override fun getLayoutId(): Int = R.layout.fragment_pin_settings

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        initViews()
        llContainer.stylizeAll()
        svContent.setBottomInsetPadding()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        updateTitle(getString(R.string.pin_settings_title))
    }

    override fun onResume() {
        super.onResume()
        initSwitches()
    }

    private fun initSwitches() {
        switchPin.onCheckedListener = null

        val hasPin = Prefs.pin.isNotBlank()
        switchPin.isChecked = hasPin
        llPinContainer.setVisible(hasPin)
        switchNotifyAboutInvader.isChecked = Prefs.notifyAboutInvaders
        switchInvaderPhoto.setVisible(switchNotifyAboutInvader.isChecked)

        switchPin.onCheckedListener = pinCheckedListener
    }

    private fun initListeners() {
        switchPin.onCheckedListener = pinCheckedListener
        btnChange.setOnClickListener {
            PinActivity.launch(context, PinActivity.Action.EDIT)
        }
        switchNotifyAboutInvader.onCheckedListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
            Prefs.notifyAboutInvaders = isChecked
            switchInvaderPhoto.setVisible(isChecked)
        }
        switchInvaderPhoto.onCheckedListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
            Prefs.takeInvaderPicture = isChecked
            if (isChecked && !hasCameraPermissions()) {
                requestCameraPermissions()
            }
        }
        switchFakeApp.onCheckedListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
            llFakeApp.setVisible(isChecked)
            if (fakeAppType == FakeAppType.NONE) {
                fakeAppType = FakeAppType.ALARMS
                rgFakeApp.check(R.id.rbAlarms)
            }
            Prefs.fakeAppType = when {
                isChecked -> fakeAppType
                else -> FakeAppType.NONE
            }
        }
        switchMixture.onCheckedListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
            llMixtures.setVisible(isChecked)
            if (mixtureType == MixtureType.NONE) {
                mixtureType = MixtureType.MINUTES_START
                rgMixture.check(R.id.rbMinutesStart)
                updateMixtureHints()
            }
            Prefs.pinMixtureType = when {
                isChecked -> mixtureType
                else -> MixtureType.NONE
            }
        }
        rgMixture.setOnCheckedChangeListener { _, checkedId ->
            mixtureType = when(checkedId) {
                R.id.rbMinutesStart -> MixtureType.MINUTES_START
                R.id.rbMinutesEnd -> MixtureType.MINUTES_END
                R.id.rbBatteryStart -> MixtureType.BATTERY_START
                R.id.rbBatteryEnd -> MixtureType.BATTERY_END
                else -> MixtureType.NONE
            }
            Prefs.pinMixtureType = mixtureType
            updateMixtureHints()
        }
        rgFakeApp.setOnCheckedChangeListener { _, checkedId ->
            fakeAppType = when(checkedId) {
                R.id.rbAlarms -> FakeAppType.ALARMS
                R.id.rbDiagnostics -> FakeAppType.DIAGNOSTICS
                else -> FakeAppType.NONE
            }
            Prefs.fakeAppType = fakeAppType
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initViews() {
        val start = getString(R.string.pin_settings_mixture_start)
        val end = getString(R.string.pin_settings_mixture_end)

        rbMinutesStart.text = "$minutes $start"
        rbMinutesEnd.text = "$minutes $end"
        rbBatteryStart.text = "$battery $start"
        rbBatteryEnd.text = "$battery $end"

        val mixtureButtonId = when(Prefs.pinMixtureType) {
            MixtureType.MINUTES_START -> R.id.rbMinutesStart
            MixtureType.MINUTES_END -> R.id.rbMinutesEnd
            MixtureType.BATTERY_START -> R.id.rbBatteryStart
            MixtureType.BATTERY_END -> R.id.rbBatteryEnd
            MixtureType.NONE -> 0
        }
        if (mixtureButtonId != 0) {
            rgMixture.check(mixtureButtonId)
        }
        switchMixture.isChecked = mixtureButtonId != 0
        updateMixtureHints()

        val fakeAppButtonId = when(Prefs.fakeAppType) {
            FakeAppType.ALARMS -> R.id.rbAlarms
            FakeAppType.DIAGNOSTICS -> R.id.rbDiagnostics
            FakeAppType.NONE -> 0
        }
        if (fakeAppButtonId != 0) {
            rgFakeApp.check(fakeAppButtonId)
        }
        switchFakeApp.isChecked = fakeAppButtonId != 0
    }

    private fun updateMixtureHints() {
        val context = context ?: return
        val minutes = getMinutes()
        val battery = getBatteryLevel(context)
        val pin = getString(R.string.pin_settings_mixture_your_pin)

        val first = when(mixtureType) {
            MixtureType.MINUTES_START -> this.minutes
            MixtureType.BATTERY_START -> this.battery
            else -> pin
        }
        val second = when(mixtureType) {
            MixtureType.MINUTES_END -> this.minutes
            MixtureType.BATTERY_END -> this.battery
            else -> pin
        }

        val explanation = getString(
                R.string.pin_settings_mixture_explanation_current,
                minutes, battery
        )
        val explanationEnter = getString(
                R.string.pin_settings_mixture_explanation_enter,
                first, second
        )
        tvMixtureHint.text = explanation
        tvMixtureEnterHint.text = explanationEnter
    }

    private fun invalidateTakePhoto() {
        val hasPermissions = hasCameraPermissions()
        if (!hasPermissions) {
            Prefs.takeInvaderPicture = false
        }
        switchInvaderPhoto.isChecked = Prefs.takeInvaderPicture
    }

    private fun hasCameraPermissions() =
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED

    private fun requestCameraPermissions() {
        requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_PERMISSIONS)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS) {
            Handler(Looper.getMainLooper()).postDelayed({
                invalidateTakePhoto()
            }, 200L)
        }
    }

    enum class MixtureType {
        NONE,
        MINUTES_START,
        MINUTES_END,
        BATTERY_START,
        BATTERY_END
    }

    enum class FakeAppType {
        NONE,
        ALARMS,
        DIAGNOSTICS
    }

    companion object {
        private const val REQUEST_PERMISSIONS = 2625
        fun newInstance() = SecurityFragment()
    }

    private inner class OnPinChecked : CompoundButton.OnCheckedChangeListener {
        override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
            if (isChecked) {
                PinActivity.launch(context, PinActivity.Action.SET)
            } else {
                PinActivity.launch(context, PinActivity.Action.RESET)
            }
        }
    }
}