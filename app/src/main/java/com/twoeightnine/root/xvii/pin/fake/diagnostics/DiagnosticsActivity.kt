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
import com.twoeightnine.root.xvii.utils.setBottomInsetPadding
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

        svContent.setBottomInsetPadding()
    }

    private fun LiveData<String>.observeInto(textView: TextView) {
        observe(this@DiagnosticsActivity) { textView.text = it }
    }

    companion object {
        fun launch(context: Context?) {
            context?.startActivity(Intent(context, DiagnosticsActivity::class.java))
        }
    }
}