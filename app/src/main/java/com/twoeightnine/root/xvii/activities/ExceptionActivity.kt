package com.twoeightnine.root.xvii.activities

import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.consts.Api
import com.twoeightnine.root.xvii.dagger.ApiService
import com.twoeightnine.root.xvii.managers.Session
import com.twoeightnine.root.xvii.utils.restartApp
import com.twoeightnine.root.xvii.utils.showCommon
import com.twoeightnine.root.xvii.utils.showError
import com.twoeightnine.root.xvii.utils.subscribeSmart
import com.twoeightnine.root.xvii.views.TextInputAlertDialog
import javax.inject.Inject

class ExceptionActivity : AppCompatActivity() {

    @BindView(R.id.tvStack)
    lateinit var tvStack: TextView
    @BindView(R.id.btnReport)
    lateinit var btnReport: Button

    companion object {
        val ERROR = "error"
    }

    @Inject
    lateinit var api: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exception)
        ButterKnife.bind(this)
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
                { sendError("CRASH REPORT:\n$it\n\n$error") },
                false
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
        api.send(-Api.GROUP, message, "", "", 0, "", "")
                .subscribeSmart({
                    response ->
                    deleteReport(response)
                    if (sendLater.isEmpty()) {
                        showCommon(this, R.string.report_sent)
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