package com.twoeightnine.root.xvii.features.appearance

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import androidx.appcompat.app.AlertDialog
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.chats.attachments.gallery.GalleryFragment
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.utils.*
import com.twoeightnine.root.xvii.views.LoadingDialog
import kotlinx.android.synthetic.main.chat_input_panel.*
import kotlinx.android.synthetic.main.fragment_appearance.*
import kotlinx.android.synthetic.main.view_appearance_sample.*

class AppearanceFragment : BaseFragment() {

    private lateinit var bottomSheetHelper: BottomSheetHelper
    private lateinit var permissionHelper: PermissionHelper

    private var isLightBefore = false
    private var colorBefore = 0
    private var currentColor = 0

    var dialog: LoadingDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        colorBefore = Prefs.color
        currentColor = colorBefore
        initViews()
        invalidateSample()
//        ivPreview.stylize(ColorManager.MAIN_TAG)
        switchLightTheme.stylize()
        rlHideBottom.stylizeColor()
        btnSelectBackground.stylize()
        pbAttach.hide()
        rlAttachCount.hide()

        etInput.isClickable = false
        etInput.isFocusable = false

        bottomSheetHelper = BottomSheetHelper(
                rlBottom,
                rlHideBottom,
                tvBottomTitle,
                R.id.flBottom,
                childFragmentManager,
                resources.getDimensionPixelSize(R.dimen.bottomsheet_height)
        )
        permissionHelper = PermissionHelper(this)
    }

    override fun getLayoutId() = R.layout.fragment_appearance

    private fun invalidateSample() {
        applyColors()
        applyTexts()
        applyVisibility()
    }

    private fun applyColors() {
        if (switchLightTheme.isChecked) {
            csThemeColor.color = currentColor
            val colors = ColorManager.getFromMain(currentColor)
            rlToolbar.setBackgroundColor(colors[1])
            rlSampleRoot.setBackgroundColor(Color.WHITE)
            rlInputBack.setBackgroundColor(Color.WHITE)
            arrayOf(ivMic, ivSend).forEach { iv ->
                iv.drawable.setColorFilter(colors[1], PorterDuff.Mode.SRC_ATOP)
            }

            arrayOf(tvBodyIn, tvBodyOut, etInput).forEach { it.setTextColor(0xff222222.toInt()) }
            arrayOf(tvDateIn, tvDateOut).forEach { it.setTextColor(0xff444444.toInt()) }
            tvSubtitle.setTextColor(0xffe3e3e3.toInt())

            (readStateDot.drawable as? GradientDrawable)?.setColor(colors[1])
            (llMessageIn.background as? GradientDrawable)?.setColor(colors[2])
            (llMessageOut.background as? GradientDrawable)?.setColor(colors[3])
        } else {
            rlToolbar.setBackgroundColor(0xff15121c.toInt())
            rlSampleRoot.setBackgroundColor(0xff0e0c13.toInt())
            rlInputBack.setBackgroundColor(0xff15121c.toInt())
            arrayOf(ivMic, ivSend).forEach { iv ->
                iv.drawable.setColorFilter(0xff7C43B8.toInt(), PorterDuff.Mode.SRC_ATOP)
            }

            arrayOf(tvBodyIn, tvBodyOut, etInput).forEach { it.setTextColor(0xffdddddd.toInt()) }
            arrayOf(tvDateIn, tvDateOut).forEach { it.setTextColor(0xffaaaaaa.toInt()) }
            tvSubtitle.setTextColor(0xffe3e3e3.toInt())

            (readStateDot.drawable as? GradientDrawable)?.setColor(Color.WHITE)
            (llMessageIn.background as? GradientDrawable)?.setColor(0xff1c1826.toInt())
            (llMessageOut.background as? GradientDrawable)?.setColor(0xff1c1826.toInt())
        }
    }

    private fun applyTexts() {
        val context = context ?: return

        val useAppleEmojis = switchAppleEmojis.isChecked
        val showSeconds = switchShowSeconds.isChecked
        val inLower = switchLowerTexts.isChecked

        val sampleIn = getString(R.string.appearance_sample_in)
        val sampleOut = getString(R.string.appearance_sample_out)
        val sampleDateIn = getTime(time() - 3647, withSeconds = showSeconds)
        val sampleDateOut = getTime(time() - 364, withSeconds = showSeconds)
        val sampleLastSeen = getLastSeenText(
                context.resources,
                isOnline = false,
                timeStamp = time() - 2147,
                deviceCode = 0,
                withSeconds = showSeconds
        )


        tvBodyIn.text = when {
            useAppleEmojis -> EmojiHelper.getEmojied(context, sampleIn)
            else -> sampleIn
        }
        tvBodyOut.text = when {
            useAppleEmojis -> EmojiHelper.getEmojied(context, sampleOut)
            else -> sampleOut
        }

        tvDateIn.text = sampleDateIn
        tvDateOut.text = sampleDateOut
        tvSubtitle.text = sampleLastSeen

        tvTitle.text = getString(R.string.appearance_sample_name)
        etInput.setText(getString(R.string.appearance_sample_input))
        if (inLower) {
            tvTitle.lower()
            etInput.lower()
        }
    }

    private fun applyVisibility() {
        val showVoice = switchShowVoice.isChecked
        val showStickers = switchShowStickers.isChecked

        ivKeyboard.setVisible(showStickers)
        ivMic.setVisible(showVoice)
        ivSend.setVisible(!showVoice)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        updateTitle(getString(R.string.appearance))
    }

    private fun initViews() {
        isLightBefore = Prefs.isLightTheme
        switchLightTheme.onCheckedListener = CompoundButton.OnCheckedChangeListener { _, b ->
            csThemeColor.setVisible(b)
            applyColors()
        }
        csThemeColor.setVisible(isLightBefore)
        switchLightTheme.isChecked = isLightBefore
        if (Prefs.chatBack.isNotEmpty()) {
            XviiPicasso.get()
                    .load("file://${Prefs.chatBack}")
                    .into(ivBackground)
        }
        btnSelectBackground.setOnClickListener { showDialog() }
        csThemeColor.setOnClickListener {
            ColorPickerDialogBuilder.with(context)
                    .initialColor(currentColor)
                    .lightnessSliderOnly()
                    .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                    .density(12)
                    .setPositiveButton(R.string.ok) { _, color, _ ->
                        currentColor = color
                        applyColors()
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .build()
                    .apply { stylize() }
                    .show()
        }

        switchShowSeconds.isChecked = Prefs.showSeconds
        switchLowerTexts.isChecked = Prefs.lowerTexts
        switchAppleEmojis.isChecked = Prefs.appleEmojis
        switchShowStickers.isChecked = Prefs.showStickers
        switchShowVoice.isChecked = Prefs.showVoice

        CompoundButton.OnCheckedChangeListener { _, _ ->
            applyTexts()
        }.apply {
            switchAppleEmojis.onCheckedListener = this
            switchLowerTexts.onCheckedListener = this
            switchShowSeconds.onCheckedListener = this
        }

        CompoundButton.OnCheckedChangeListener { _, _ ->
            applyVisibility()
        }.apply {
            switchShowStickers.onCheckedListener = this
            switchShowVoice.onCheckedListener = this
        }

    }

    private fun openGallery() {
        permissionHelper.doOrRequest(
                arrayOf(PermissionHelper.READ_STORAGE, PermissionHelper.WRITE_STORAGE),
                R.string.no_access_to_storage,
                R.string.need_access_to_storage
        ) {
            bottomSheetHelper.openBottomSheet(GalleryFragment.newInstance {
                bottomSheetHelper.closeBottomSheet()
                if (it.isNotEmpty()) {
                    convertPhoto(it[0].path)
                } else {
                    showError(activity, R.string.error)
                }
            }, getString(R.string.gallery))
        }
    }

    private fun showDialog() {
        val context = context ?: return

        if (Prefs.chatBack.isNotEmpty()) {
            val dialog = AlertDialog.Builder(context)
                    .setMessage(R.string.chat_back_exists)
                    .setPositiveButton(R.string.change) { _, _ -> openGallery() }
                    .setNegativeButton(R.string.delete) { _, _ -> deletePhoto() }
                    .create()
            dialog.show()
            dialog.stylize()
        } else {
            openGallery()
        }
    }

    private fun deletePhoto() {
        ivBackground.setImageBitmap(null)
        Prefs.chatBack = ""
    }

    private fun convertPhoto(path: String) {
        val activity = activity ?: return

        val newPath = getCroppedImagePath(activity, path)
        Prefs.chatBack = newPath
        hideDialog(newPath)
    }

    private fun hideDialog(newPath: String) {
        XviiPicasso.get()
                .load("file://$newPath")
                .into(ivBackground)
    }

    override fun onStop() {
        super.onStop()
        GalleryFragment.clear()

        Prefs.showSeconds = switchShowSeconds.isChecked
        Prefs.lowerTexts = switchLowerTexts.isChecked
        Prefs.appleEmojis = switchAppleEmojis.isChecked
        Prefs.showStickers = switchShowStickers.isChecked
        Prefs.showVoice = switchShowVoice.isChecked
    }

    /**
     * for parent activity
     */
    fun hasChanges() = isLightBefore != switchLightTheme.isChecked
            || switchLightTheme.isChecked && currentColor != colorBefore

    /**
     * for parent activity
     */
    fun askForRestarting() {
        showConfirm(context, getString(R.string.wanna_change_theme)) { yes ->
            if (yes) {
                Prefs.color = currentColor
                Prefs.isLightTheme = switchLightTheme.isChecked
                restartApp(context, getString(R.string.theme_changed))
            } else {
                switchLightTheme.isChecked = isLightBefore
                currentColor = colorBefore
                activity?.onBackPressed()
            }
        }
    }

    companion object {

        fun newInstance() = AppearanceFragment()
    }
}