package com.twoeightnine.root.xvii.activities

import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.BuildConfig
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.utils.*
import com.twoeightnine.root.xvii.views.TextInputAlertDialog
import kotlinx.android.synthetic.main.activity_exception.*
import javax.inject.Inject

class ExceptionActivity : AppCompatActivity() {

    companion object {
        const val ERROR = "error"
    }

    @Inject
    lateinit var api: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        NightModeHelper.updateConfig(
                if (Prefs.isNight) {
                    Configuration.UI_MODE_NIGHT_YES
                } else {
                    Configuration.UI_MODE_NIGHT_NO
                },
                this, R.style.AppTheme
        )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exception)
        App.appComponent?.inject(this)
        var error = ""
        if (intent.extras != null) {
            error = intent.extras.getString(ERROR)
            tvStack.text = error
        }
        btnReport.setOnClickListener {
            showDialog(error)

        }
    }

    private fun showDialog(error: String) {
        val dialog = TextInputAlertDialog(
                this,
                getString(R.string.describe_actions),
                getString(R.string.describe_hint), "",
                { sendError("NEW CRASH IN ${BuildConfig.VERSION_NAME}:\n$it\n$error") }
        )
        dialog.show()
    }

    private fun sendError(error: String) {
        val maxSize = 3000
        var message = error
        var sendLater = ""
        if (message.length > maxSize) {
            sendLater = message.substring(maxSize)
            message = message.substring(0, maxSize)
        }
        api.send(-App.GROUP, message, "", "", 0, "", "")
                .subscribeSmart({ response ->
                    deleteReport(response)
                    if (sendLater.isEmpty()) {
                        showToast(this, R.string.report_sent)
                        Handler().postDelayed({ onBackPressed() }, 200L)
                    } else {
                        sendError(sendLater)
                    }
                }, {
                    showError(this, it)
                })
    }

    private fun deleteReport(mid: Int) {
        api.deleteMessages("$mid", 0)
                .subscribeSmart({}, {})
    }

    override fun onBackPressed() {
        super.onBackPressed()
        restartApp()
    }
}