package com.twoeightnine.root.xvii.pin

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.AndroidRuntimeException
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseActivity
import com.twoeightnine.root.xvii.lg.L
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.managers.Session
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.utils.*
import com.twoeightnine.root.xvii.views.PinPadView
import kotlinx.android.synthetic.main.activity_pin.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import javax.inject.Inject
import kotlin.random.Random


/**
 * Created by root on 3/17/17.
 */

class PinActivity : BaseActivity() {

    @Inject
    lateinit var api: ApiService

    private val action by lazy {
        intent?.extras?.getSerializable(ACTION) as? Action
    }
    private var currentStage: Action? = null

    private var pin = ""
    private var confirmedPin = ""

    private var correctPin: String? = null
    private var failedPrompts: Int = 0

    private val mainHandler = Handler(Looper.getMainLooper())

    private var camera: SimpleCamera? = null
    private val photoFile by lazy {
        File(filesDir, "invader.jpg")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.appComponent?.inject(this)
        setContentView(R.layout.activity_pin)

        action ?: finish()
        init()
        rlContainer.stylizeAll()
        rlPinControls.setBottomInsetPadding()

        ivBack.setVisible(action != Action.ENTER)
        ivBack.setOnClickListener { onBackPressed() }
        ivBack.setTopInsetMargin()

        if (Session.needToWaitAfterFailedPin()) {
            showBruteForced(justNow = false)
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
                pin += key
                tvPinDots.text = "${tvPinDots.text}●"
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
                val errorStringRes = when (PinUtils.getPinWeakness(pin)) {
                    PinUtils.PinWeakness.NONE -> {
                        tvTitle.setText(R.string.confirm_pin)
                        currentStage = Action.CONFIRM
                        confirmedPin = pin
                        0
                    }
                    PinUtils.PinWeakness.LENGTH -> R.string.pin_not_secure_length
                    PinUtils.PinWeakness.PATTERN -> R.string.pin_not_secure_pattern
                    PinUtils.PinWeakness.YEAR -> R.string.pin_not_secure_year
                    PinUtils.PinWeakness.DATE -> R.string.pin_not_secure_date
                }
                if (errorStringRes != 0) {
                    val error = StringBuilder()
                            .append(getString(R.string.pin_not_secure_general))
                            .append("\n")
                            .append(getString(errorStringRes))
                            .toString()
                    showError(error)
                }
            }

            Action.CONFIRM -> {
                if (pin == confirmedPin) {
                    showToast(this, R.string.updated_succ)
                    Prefs.pin = PinUtils.getPinHash(pin)
                    Session.pinLastPromptResult = time()
                    l("pin set")
                    finish()
                } else {
                    currentStage = Action.SET
                    tvTitle.setText(R.string.enter_new_pin)
                    showError(getString(R.string.dont_match))
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
            showBruteForced(justNow = true)
        }
        showError(getString(R.string.incorrect_pin))
        l("pin is incorrect")
    }

    private fun isPinCorrect(): Boolean =
            PinUtils.isPinCorrect(
                    pin = pin,
                    correctPinHash = correctPin ?: "",
                    mixtureType = Prefs.pinMixtureType,
                    minutes = getMinutes(),
                    battery = getBatteryLevel(this)
            )

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
                l("pin reset")
                finish()
            }

            Action.EDIT -> {
                tvTitle.setText(R.string.enter_new_pin)
                if (Prefs.pinMixtureType != SecurityFragment.MixtureType.NONE) {
                    tvMixtureHint.show()
                }
                currentStage = Action.SET
            }
        }
    }

    private fun showBruteForced(justNow: Boolean = true) {
        val notify = Prefs.notifyAboutInvaders
        val notifyWithPhoto = notify && Prefs.takeInvaderPicture
        rlBruteForce.show()
        if (justNow) {
            when {
                notifyWithPhoto -> captureInvader()
                notify -> sendNotify()
            }
        }
    }

    private fun captureInvader() {
        camera = SimpleCamera(
                textureView,
                photoFile,
                CameraDelegate()
        )
        camera?.start()
        postCamera {
            camera?.takePicture()
        }
    }

    // i'm sorry but i'm lazy
    private fun uploadPhoto(file: File) {
        api.getPhotoUploadServer()
                .subscribeSmart({ uploadServer ->
                    val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
                    val body = MultipartBody.Part.createFormData("photo", file.name, requestFile)
                    api.uploadPhoto(uploadServer.uploadUrl ?: "", body)
                            .compose(applySchedulers())
                            .subscribe({ uploaded ->
                                api.saveMessagePhoto(
                                        uploaded.photo ?: "",
                                        uploaded.hash ?: "",
                                        uploaded.server
                                )
                                        .subscribeSmart({ photos ->
                                            val photoId = photos.getOrNull(0)?.photoId
                                                    ?.let { "photo$it" }
                                            sendNotify(photoId)
                                        }, { error ->
                                            sendNotify()
                                            lw("save uploaded photo error: $error")
                                        })
                            }, { error ->
                                lw("unable to upload photo", error)
                                sendNotify()
                            })

                }, { error ->
                    sendNotify()
                    lw("getting ploading server error: $error")
                })
    }

    @SuppressLint("CheckResult")
    private fun sendNotify(photoId: String? = null) {
        api.sendMessage(
                peerId = Session.uid,
                randomId = Random.nextInt(),
                text = getString(R.string.pin_invader_notification),
                attachments = photoId
        )
                .compose(applySchedulers())
                .subscribe({
                    l("invader notification sent")
                }, { throwable ->
                    lw("unable to send invader notification", throwable)
                })
    }

    private fun showError(text: String) {
        tvError.text = text
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

    private fun l(s: String) {
        L.tag(TAG).log(s)
    }

    private fun lw(s: String, throwable: Throwable? = null) {
        L.tag(TAG)
                .throwable(throwable)
                .log(s)
    }

    private fun postCamera(block: () -> Unit) {
        mainHandler.postDelayed(block, CAMERA_DELAY)
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

        private const val CAMERA_DELAY = 1000L

        fun launch(context: Context?, action: Action) {
            context ?: return

            try {
                context.startActivity(Intent(context, PinActivity::class.java).apply {
                    putExtra(ACTION, action)
                })
            } catch (e: AndroidRuntimeException) {
                L.tag(TAG)
                        .throwable(e)
                        .log("error launching pin with $action")
            }
        }

        private const val TAG = "pin"

        private const val ALLOWED_PROMPTS = 5

        const val ACTION = "action"
    }

    private inner class CameraDelegate : SimpleCamera.ControllerDelegate {

        override fun requireActivity(): Activity = this@PinActivity

        override fun onErrorOccurred(explanation: String, throwable: Throwable?) {
            sendNotify()
            lw(explanation, throwable)
        }

        override fun onPictureTaken(file: File) {
            uploadPhoto(file)
            postCamera {
                textureView.hide()
                camera?.stop()
            }
        }

        override fun onPreviewRatioUpdated(wToH: Float) {
            if (wToH == 0f) return
            cvPhoto.layoutParams?.apply {
                height = (width / wToH).toInt()
                cvPhoto.layoutParams = this
            }
        }
    }
}
