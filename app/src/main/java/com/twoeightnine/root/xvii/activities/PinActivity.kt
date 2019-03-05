package com.twoeightnine.root.xvii.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.AndroidRuntimeException
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.managers.Lg
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.managers.Session
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.model.Account
import com.twoeightnine.root.xvii.utils.*
import com.twoeightnine.root.xvii.views.PinPadView
import io.realm.Realm

/**
 * Created by root on 3/17/17.
 */

class PinActivity : BaseActivity() {

    private var action: String? = null
    private var currentStage: String? = null

    @BindView(R.id.tvTitle)
    lateinit var tvTitle: TextView
    @BindView(R.id.tvPinDots)
    lateinit var tvPinDots: TextView
    @BindView(R.id.pinPad)
    lateinit var pinPad: PinPadView
    @BindView(R.id.tvForgot)
    lateinit var tvForgot: TextView
    @BindView(R.id.rlContainer)
    lateinit var rlContainer: RelativeLayout

    private var pin = ""
    private var confirmedPin = ""

    private var correctPin: String? = null
    private var failedPrompts: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin)
        ButterKnife.bind(this)
        if (intent.extras != null) {
            action = intent.extras.getString(ACTION)
        } else {
            return
        }
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
                if (currentStage == ACTION_ENTER && action == ACTION_ENTER && isPinCorrect()) {
                    onCorrect()
                }
            }
        }
    }

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
                showCommon(this, R.string.updated_succ)
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
                showCommon(this, R.string.reset_succ)
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
                .setPositiveButton(android.R.string.ok) { _, _ -> resetPin() }
                .setNegativeButton(android.R.string.cancel, null)
                .create()

        dialog.show()
        Style.forDialog(dialog)
    }

    private fun resetPin() {
        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        realm.where(Account::class.java)
                .findAll()
                .deleteAllFromRealm()
        realm.commitTransaction()
        Session.token = ""
        Prefs.pin = ""
        restartApp(getString(R.string.restart_app))
    }

    private fun resetInput() {
        pin = ""
        tvPinDots.text = ""
    }

    private fun init() {
        pinPad.listener = { onPin(it) }
        tvForgot.visibility = View.INVISIBLE
        if (action == ACTION_ENTER) pinPad.hideOk()

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
