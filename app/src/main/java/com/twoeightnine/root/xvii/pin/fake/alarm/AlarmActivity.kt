package com.twoeightnine.root.xvii.pin.fake.alarm

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseActivity
import com.twoeightnine.root.xvii.lg.L
import com.twoeightnine.root.xvii.main.MainActivity
import com.twoeightnine.root.xvii.utils.AppBarLifter
import com.twoeightnine.root.xvii.utils.goHome
import global.msnthrp.xvii.uikit.extensions.applyBottomInsetMargin
import global.msnthrp.xvii.uikit.extensions.applyBottomInsetPadding
import kotlinx.android.synthetic.main.activity_alarms.*

class AlarmActivity : BaseActivity() {

    private val alarms = createDefaultAlarms()

    private val adapter by lazy {
        AlarmAdapter(this) {
            MainActivity.launch(this)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarms)
        rvAlarms.layoutManager = LinearLayoutManager(this)
        rvAlarms.adapter = adapter
        rvAlarms.addOnScrollListener(AppBarLifter(xviiToolbar))
        adapter.update(createDefaultAlarms())
        rvAlarms.addOnScrollListener(FabVisibilityWatcher())

        fabAdd.setOnClickListener {
            TimePickerDialog(this, { _, hour, minute ->
                alarms.add(Alarm(hour * 60 + minute, true))
                alarms.sortBy { it.time }
                adapter.update(alarms)
            }, 9, 17, true).show()
        }

        rvAlarms.applyBottomInsetPadding()
        fabAdd.applyBottomInsetMargin()
    }

    override fun onBackPressed() {
        try {
            goHome(this)
        } catch (e: Exception) {
            L.tag("alarm")
                    .throwable(e)
                    .log("unable to go home")
        }
    }

    override fun shouldRunService(): Boolean = false

    private fun createDefaultAlarms() = arrayListOf(
            Alarm(450, false, enabled = false),
            Alarm(480, true, enabled = false),
            Alarm(490, true, enabled = false),
            Alarm(500, false, enabled = true),
            Alarm(510, false, enabled = true),
            Alarm(525, true, enabled = true),
            Alarm(540, false, enabled = true),
            Alarm(600, false, enabled = false),
            Alarm(660, false, enabled = false),
            Alarm(800, true, enabled = false)
    )

    companion object {

        fun launch(context: Context?) {
            context?.startActivity(Intent(context, AlarmActivity::class.java))
        }
    }

    private inner class FabVisibilityWatcher : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (dy < 0) {
                fabAdd.show()
            } else {
                fabAdd.hide()
            }
        }
    }
}