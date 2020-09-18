package com.twoeightnine.root.xvii.pin

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.utils.setVisible
import com.twoeightnine.root.xvii.utils.stylizeAll
import kotlinx.android.synthetic.main.fragment_pin_settings.*

class PinSettingsFragment : BaseFragment() {

    private val pinCheckedListener = OnPinChecked()

    override fun getLayoutId(): Int = R.layout.fragment_pin_settings

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
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