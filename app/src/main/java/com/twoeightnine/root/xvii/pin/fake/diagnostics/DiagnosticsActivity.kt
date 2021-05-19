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

package com.twoeightnine.root.xvii.pin.fake.diagnostics

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProviders
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseActivity
import com.twoeightnine.root.xvii.main.MainActivity
import global.msnthrp.xvii.uikit.extensions.applyBottomInsetPadding
import kotlinx.android.synthetic.main.activity_diagnostics.*

class DiagnosticsActivity : BaseActivity() {

    private val viewModel by lazy {
        ViewModelProviders.of(this)[DiagnosticsViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diagnostics)
        viewModel.battery.observeInto(tvBatteryTest)
        viewModel.cpu.observeInto(tvCpuTest)
        viewModel.ram.observeInto(tvRamTest)
        viewModel.network.observeInto(tvNetworkTest)
        viewModel.display.observeInto(tvDisplayTest)

        btnDisplay.setOnClickListener {
            viewModel.runDisplay(this)
        }
        btnNetwork.setOnClickListener {
            viewModel.runNetwork()
        }
        btnCpu.setOnClickListener {
            viewModel.runCpu()
        }
        btnRam.setOnClickListener {
            viewModel.runRam()
        }
        btnBattery.setOnClickListener {
            viewModel.runBattery(this)
        }

        btnBattery.setOnLongClickListener {
            MainActivity.launch(this)
            finish()
            true
        }
        viewModel.runAll(this)
        Handler(Looper.getMainLooper()).postDelayed({
            tvStability.text = viewModel.getStability().toString()
        }, 2000L)

        svContent.applyBottomInsetPadding()
    }

    override fun shouldRunService(): Boolean = false

    private fun LiveData<String>.observeInto(textView: TextView) {
        observe(this@DiagnosticsActivity) { textView.text = it }
    }

    companion object {
        fun launch(context: Context?) {
            context?.startActivity(Intent(context, DiagnosticsActivity::class.java))
        }
    }
}