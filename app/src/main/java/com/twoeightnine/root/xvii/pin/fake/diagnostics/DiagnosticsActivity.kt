package com.twoeightnine.root.xvii.pin.fake.diagnostics

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseActivity
import com.twoeightnine.root.xvii.main.MainActivity
import kotlinx.android.synthetic.main.activity_diagnostics.*

class DiagnosticsActivity : BaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diagnostics)
        btnBattery.setOnClickListener {

        }
        btnBattery.setOnLongClickListener {
            MainActivity.launch(this)
            finish()
            true
        }
    }

    companion object {
        fun launch(context: Context?) {
            context?.startActivity(Intent(context, DiagnosticsActivity::class.java))
        }
    }
}