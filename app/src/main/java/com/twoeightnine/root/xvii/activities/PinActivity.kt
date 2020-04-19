package com.twoeightnine.root.xvii.activities

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.AndroidRuntimeException
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.crypto.sha256
import com.twoeightnine.root.xvii.db.AppDb
import com.twoeightnine.root.xvii.lg.Lg
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.managers.Session
import com.twoeightnine.root.xvii.utils.*
import com.twoeightnine.root.xvii.views.PinPadView
import kotlinx.android.synthetic.main.activity_pin.*
import javax.inject.Inject

/**
 * Created by root on 3/17/17.
 */

class PinActivity : BaseActivity() {

    @Inject
    lateinit var appDb: AppDb

    private val action by lazy { intent?.extras?.getString(ACTION) }
    private var currentStage: String? = null

    private var pin = ""
    private var confirmedPin = ""

    private var correctPin: String? = null
    private var failedPrompts: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.appComponent?.inject(this)
        setContentView(R.layout.activity_pin)

        action ?: finish()
        init()
        styleScreen(rlContainer)
    }

    private fun onPin(key: Int) {
        when (key) {
            PinPadView.DELETE -> {
                pin = ""
                tvPinDots.text = ""
            }

            PinPadView.OK -> onOkPressed()

            else -> {
                if (pin.length < LENGTH) {
                    pin += key
                    tvPinDots.text = "${tvPinDots.text}●"
                }
            }
        }
    }

    override fun getNavigationBarColor() = Color.TRANSPARENT

    private fun onOkPressed() {
        when (currentStage) {
            ACTION_ENTER -> if (isPinCorrect()) {
                onCorrect()
            } else {
                onIncorrect()
            }

            ACTION_SET -> {
                tvTitle.setText(R.string.confirm_pin)
                currentStage = ACTION_CONFIRM
                confirmedPin = pin
            }

            ACTION_CONFIRM -> if (pin == confirmedPin) {
                showToast(this, R.string.updated_succ)
                Prefs.pin = sha256("$pin$SALT")
                Session.pinLastPromptResult = time()
                finish()
            } else {
                currentStage = ACTION_SET
                tvTitle.setText(R.string.enter_new_pin)
                showError(this, R.string.dont_match)
            }
        }
        resetInput()
    }

    private fun onIncorrect() {
        failedPrompts++
        if (failedPrompts >= PROMPTS && action == ACTION_ENTER) {
            tvForgot.visibility = View.VISIBLE
        }
        showError(this, R.string.incorrect_pin)
    }

    private fun isPinCorrect() = correctPin == sha256("$pin$SALT")

    private fun onCorrect() {
        when (action) {

            ACTION_ENTER -> {
                Session.pinLastPromptResult = time()
                finish()
            }

            ACTION_RESET -> {
                Prefs.pin = ""
                showToast(this, R.string.reset_succ)
                finish()
            }

            ACTION_EDIT -> {
                tvTitle.setText(R.string.enter_new_pin)
                currentStage = ACTION_SET
            }
        }
    }

    private fun showResetDialog() {
        val dialog = AlertDialog.Builder(this)
                .setTitle(R.string.reset_pin)
                .setMessage(R.string.reset_pin_desc)
                .setPositiveButton(R.string.ok) { _, _ -> resetPin() }
                .setNegativeButton(R.string.cancel, null)
                .create()

        dialog.show()
        dialog.stylize()
    }

    private fun resetPin() {
        appDb.clearAsync()
        Session.token = ""
        Prefs.pin = ""
        restartApp(this, getString(R.string.restart_app))
    }

    private fun resetInput() {
        pin = ""
        tvPinDots.text = ""
    }

    private fun init() {
        pinPad.listener = { onPin(it) }
        tvForgot.visibility = View.INVISIBLE

        when (action) {
            ACTION_SET -> {
                tvTitle.setText(R.string.enter_new_pin)
                currentStage = ACTION_SET
            }

            ACTION_EDIT, ACTION_ENTER, ACTION_RESET -> {
                tvTitle.setText(R.string.enter_pin)
                correctPin = Prefs.pin
                currentStage = ACTION_ENTER
                tvForgot.setOnClickListener { showResetDialog() }
            }
        }
    }

    override fun onBackPressed() {
        if (action != ACTION_ENTER) {
            super.onBackPressed()
        }
    }

    companion object {

        fun launch(context: Context?, action: String) {
            context ?: return

            try {
                context.startActivity(Intent(context, PinActivity::class.java).apply {
                    putExtra(ACTION, action)
                })
            } catch (e: AndroidRuntimeException) {
                e.printStackTrace()
                Lg.wtf("error launching pin with $action: ${e.message}")
            }
        }

        private const val PROMPTS = 2

        const val ACTION = "action"
        const val ACTION_SET = "actionSet"
        const val ACTION_ENTER = "actionEnter"
        const val ACTION_EDIT = "actionEdit"
        const val ACTION_RESET = "actionReset"
        const val ACTION_CONFIRM = "actionConfirm"

        private const val LENGTH = 8
        private const val SALT = "oi|6yw4-c5g846-d5c53s9mx"
    }
}
