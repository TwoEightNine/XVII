package com.twoeightnine.root.xvii.pin

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.utils.getBatteryLevel
import com.twoeightnine.root.xvii.utils.getMinutes
import com.twoeightnine.root.xvii.utils.setVisible
import com.twoeightnine.root.xvii.utils.stylizeAll
import kotlinx.android.synthetic.main.fragment_pin_settings.*

class PinSettingsFragment : BaseFragment() {

    private val pinCheckedListener = OnPinChecked()

    private val minutes by lazy { getString(R.string.pin_settings_mixture_minutes) }
    private val battery by lazy { getString(R.string.pin_settings_mixture_battery) }

    private var mixtureType = MixtureType.NONE

    override fun getLayoutId(): Int = R.layout.fragment_pin_settings

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        initViews()
        llContainer.stylizeAll()
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

        switchPin.onCheckedListener = pinCheckedListener
    }

    private fun initListeners() {
        switchPin.onCheckedListener = pinCheckedListener
        btnChange.setOnClickListener {
            PinActivity.launch(context, PinActivity.Action.EDIT)
        }
        switchNotifyAboutInvader.onCheckedListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
            Prefs.notifyAboutInvaders = isChecked
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
    }

    @SuppressLint("SetTextI18n")
    private fun initViews() {
        val start = getString(R.string.pin_settings_mixture_start)
        val end = getString(R.string.pin_settings_mixture_end)

        rbMinutesStart.text = "$minutes $start"
        rbMinutesEnd.text = "$minutes $end"
        rbBatteryStart.text = "$battery $start"
        rbBatteryEnd.text = "$battery $end"

        val radioButtonId = when(Prefs.pinMixtureType) {
            MixtureType.MINUTES_START -> R.id.rbMinutesStart
            MixtureType.MINUTES_END -> R.id.rbMinutesEnd
            MixtureType.BATTERY_START -> R.id.rbBatteryStart
            MixtureType.BATTERY_END -> R.id.rbBatteryEnd
            MixtureType.NONE -> 0
        }
        if (radioButtonId != 0) {
            rgMixture.check(radioButtonId)
        }
        switchMixture.isChecked = radioButtonId != 0
        updateMixtureHints()
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

    enum class MixtureType {
        NONE,
        MINUTES_START,
        MINUTES_END,
        BATTERY_START,
        BATTERY_END
    }

    companion object {
        fun newInstance() = PinSettingsFragment()
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