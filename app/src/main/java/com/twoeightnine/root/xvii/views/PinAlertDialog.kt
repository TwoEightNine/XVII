package com.twoeightnine.root.xvii.views

import android.content.Context
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.managers.Session
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.model.Account
import com.twoeightnine.root.xvii.utils.*
import io.realm.Realm

class PinAlertDialog(context: Context,
                     private var action: String) : AlertDialog(context) {

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

    init {
        val view = View.inflate(context, R.layout.dialog_pin, null)
        ButterKnife.bind(this, view)
        setView(view)

        init()
        Style.forAll(rlContainer)
        setCancelable(false)
    }

    private fun onPin(key: Int) {
        when (key) {
            PinPadView.DELETE -> {
                pin = ""
                tvPinDots.text = ""
            }

            PinPadView.OK -> onOkPressed()

            else -> if (pin.length < LENGTH) {
                pin += key
                tvPinDots.text = "${tvPinDots.text}●"
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
                showCommon(context, R.string.updated_succ)
                Prefs.pin = sha256("$pin$SALT")
                dismiss()
            } else {
                currentStage = ACTION_SET
                tvTitle.setText(R.string.enter_new_pin)
                showError(context, R.string.dont_match)
            }
        }
        resetInput()
    }

    private fun onIncorrect() {
        failedPrompts++
        if (failedPrompts >= PROMPTS && action == ACTION_ENTER) {
            tvForgot.visibility = View.VISIBLE
        }
        showError(context, R.string.incorrect_pin)
    }

    private fun isPinCorrect() = correctPin == sha256("$pin$SALT")

    private fun onCorrect() {
        when (action) {

            ACTION_ENTER -> {
                Session.pinLastPromptResult = time()
                dismiss()
            }

            ACTION_RESET -> {
                Prefs.pin = ""
                showCommon(context, R.string.reset_succ)
                dismiss()
            }

            ACTION_EDIT -> {
                tvTitle.setText(R.string.enter_new_pin)
                currentStage = ACTION_SET
            }
        }
    }

    private fun showResetDialog() {
        val dialog = AlertDialog.Builder(context)
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
        restartApp(context.getString(R.string.restart_app))
    }

    private fun resetInput() {
        pin = ""
        tvPinDots.text = ""
    }

    fun init() {
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


    companion object {

        private const val PROMPTS = 2

        const val ACTION_SET = "actionSet"
        const val ACTION_ENTER = "actionEnter"
        const val ACTION_EDIT = "actionEdit"
        const val ACTION_RESET = "actionReset"
        const val ACTION_CONFIRM = "actionConfirm"

        private const val LENGTH = 8
        private const val SALT = "oi|6yw4-c5g846-d5c53s9mx"
    }

}