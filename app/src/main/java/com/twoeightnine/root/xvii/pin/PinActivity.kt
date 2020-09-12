package com.twoeightnine.root.xvii.pin

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.AndroidRuntimeException
import androidx.annotation.StringRes
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.activities.BaseActivity
import com.twoeightnine.root.xvii.crypto.sha256
import com.twoeightnine.root.xvii.db.AppDb
import com.twoeightnine.root.xvii.lg.L
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

    private val action by lazy {
        intent?.extras?.getSerializable(ACTION) as? Action
    }
    private var currentStage: Action? = null

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

        ivBack.setVisible(action != Action.ENTER)
        ivBack.setOnClickListener { onBackPressed() }
        ivBack.setTopInsetMargin()

        if (Session.needToWaitAfterFailedPin()) {
            showBruteForced()
        }

//        AlarmActivity.launch(this)
    }

    private fun onPin(key: Int) {
        when (key) {
            PinPadView.DELETE -> {
                pin = ""
                tvPinDots.text = ""
            }

            PinPadView.OK -> onOkPressed()

            else -> {
                tvError.text = ""
                if (pin.length < MAX_LENGTH) {
                    pin += key
                    tvPinDots.text = "${tvPinDots.text}●"
                }
            }
        }
    }

    override fun getNavigationBarColor() = Color.TRANSPARENT

    private fun onOkPressed() {
        when (currentStage) {
            Action.ENTER -> {
                if (isPinCorrect()) {
                    onCorrect()
                } else {
                    onIncorrect()
                }
            }

            Action.SET -> {
                tvTitle.setText(R.string.confirm_pin)
                currentStage = Action.CONFIRM
                confirmedPin = pin
            }

            Action.CONFIRM -> {
                if (pin == confirmedPin) {
                    showToast(this, R.string.updated_succ)
                    Prefs.pin = sha256("$pin$SALT")
                    Session.pinLastPromptResult = time()
                    finish()
                } else {
                    currentStage = Action.SET
                    tvTitle.setText(R.string.enter_new_pin)
                    showError(R.string.dont_match)
                }
            }
        }
        resetInput()
    }

    private fun onIncorrect() {
        failedPrompts++
        Session.pinLastFailedPrompt = time()

        val denyEntrance = Session.pinBruteForced
                || failedPrompts >= ALLOWED_PROMPTS
        if (action == Action.ENTER && denyEntrance) {
            Session.pinBruteForced = true
            showBruteForced()
        }
        showError(R.string.incorrect_pin)
    }

    private fun isPinCorrect() = correctPin == sha256("$pin$SALT")

    private fun onCorrect() {
        when (action) {

            Action.ENTER -> {
                Session.pinBruteForced = false
                Session.pinLastPromptResult = time()
                finish()
            }

            Action.RESET -> {
                Prefs.pin = ""
                showToast(this, R.string.reset_succ)
                finish()
            }

            Action.EDIT -> {
                tvTitle.setText(R.string.enter_new_pin)
                currentStage = Action.SET
            }
        }
    }

    private fun showBruteForced() {
        rlBruteForce.show()
    }

    private fun showError(@StringRes textRes: Int) {
        tvError.setText(textRes)
    }

    private fun resetInput() {
        pin = ""
        tvPinDots.text = ""
    }

    private fun init() {
        pinPad.listener = { onPin(it) }

        when (action) {
            Action.SET -> {
                tvTitle.setText(R.string.enter_new_pin)
                currentStage = Action.SET
            }

            Action.EDIT, Action.ENTER, Action.RESET -> {
                tvTitle.setText(R.string.enter_pin)
                correctPin = Prefs.pin
                currentStage = Action.ENTER
            }
        }
    }

    override fun onBackPressed() {
        if (action != Action.ENTER) {
            super.onBackPressed()
        }
    }

    /**
     * type of action pin is launched for
     */
    enum class Action {
        SET,
        ENTER,
        EDIT,
        RESET,
        CONFIRM
    }

    companion object {

        fun launch(context: Context?, action: Action) {
            context ?: return

            try {
                context.startActivity(Intent(context, PinActivity::class.java).apply {
                    putExtra(ACTION, action)
                })
            } catch (e: AndroidRuntimeException) {
                e.printStackTrace()
                L.tag("pin")
                        .throwable(e)
                        .log("error launching pin with $action")
            }
        }

        private const val ALLOWED_PROMPTS = 5

        const val ACTION = "action"

        private const val MAX_LENGTH = 8
        private const val SALT = "oi|6yw4-c5g846-d5c53s9mx"
    }
}
