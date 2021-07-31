/*
 * xvii - messenger for vk
 * Copyright (C) 2021  TwoEightNine
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.twoeightnine.root.xvii.features.appearance

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.CompoundButton
import androidx.core.content.ContextCompat
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.chats.attachments.gallery.GalleryFragment
import com.twoeightnine.root.xvii.extensions.load
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.uikit.Munch
import com.twoeightnine.root.xvii.uikit.paint
import com.twoeightnine.root.xvii.utils.*
import com.twoeightnine.root.xvii.views.LoadingDialog
import global.msnthrp.xvii.uikit.extensions.applyBottomInsetPadding
import global.msnthrp.xvii.uikit.extensions.hide
import global.msnthrp.xvii.uikit.extensions.lowerIf
import global.msnthrp.xvii.uikit.extensions.setVisible
import global.msnthrp.xvii.uikit.utils.color.ColorUtils
import kotlinx.android.synthetic.main.chat_input_panel.*
import kotlinx.android.synthetic.main.fragment_appearance.*
import kotlinx.android.synthetic.main.view_appearance_sample.*
import kotlinx.android.synthetic.main.view_appearance_sample.view.*

class AppearanceFragment : BaseFragment() {

    private val mainTextLight by lazy {
        ContextCompat.getColor(requireContext(), R.color.main_text_light)
    }
    private val otherTextLight by lazy {
        ContextCompat.getColor(requireContext(), R.color.other_text_light)
    }
    private val minorTextLight by lazy {
        ContextCompat.getColor(requireContext(), R.color.minor_text_light)
    }
    private val mainTextDark by lazy {
        ContextCompat.getColor(requireContext(), R.color.main_text_dark)
    }
    private val otherTextDark by lazy {
        ContextCompat.getColor(requireContext(), R.color.other_text_dark)
    }
    private val minorTextDark by lazy {
        ContextCompat.getColor(requireContext(), R.color.minor_text_dark)
    }
    private val backgroundLight by lazy {
        ContextCompat.getColor(requireContext(), R.color.background_light)
    }
    private val backgroundDark by lazy {
        ContextCompat.getColor(requireContext(), R.color.background_dark)
    }
    private val backgroundDarkLighter by lazy {
        ContextCompat.getColor(requireContext(), R.color.background_dark_lighter)
    }
    private val messageBackgroundLight by lazy {
        ContextCompat.getColor(requireContext(), R.color.background_message_light)
    }
    private val messageBackgroundDark by lazy {
        ContextCompat.getColor(requireContext(), R.color.background_message_dark)
    }

    private lateinit var bottomSheetHelper: BottomSheetHelper
    private lateinit var permissionHelper: PermissionHelper

    private var isLightBefore = false
    private var colorBefore = 0
    private var currentColor = 0

    var dialog: LoadingDialog? = null

    override fun getLayoutId() = R.layout.fragment_appearance

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        colorBefore = Prefs.color
        currentColor = colorBefore
        initViews()
        invalidateSample()
        rlHideBottom.paint(Munch.color.color)
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

        svContent.applyBottomInsetPadding()
        rlBottom.applyBottomInsetPadding()
    }

    private fun invalidateSample() {
        applyColors()
        applyTexts()
        applyVisibility()
    }

    private fun applyColors() {
        val color = Munch.ColorScope(currentColor)
        csThemeColor.color = currentColor

        arrayOf(ivMic, ivSend, ivBackSample, readStateDot).forEach { iv ->
            iv.drawable.paint(color.color)
        }

        if (switchLightTheme.isChecked) {

            rlToolbar.setBackgroundColor(backgroundLight)
            rlSampleRoot.setBackgroundColor(backgroundLight)
            rlInputBack.setBackgroundColor(backgroundLight)

            arrayOf(tvTitle, tvBodyIn, tvBodyOut, etInput).forEach { it.setTextColor(mainTextLight) }
            arrayOf(tvDateIn, tvDateOut).forEach { it.setTextColor(otherTextLight) }
            tvSubtitle.setTextColor(minorTextLight)

            arrayOf(ivKeyboard, ivAttach).forEach { it.paint(color.colorWhite(50)) }
            (llMessageIn.background as? GradientDrawable)
                    ?.setColor(messageBackgroundLight)
            (llMessageOut.background as? GradientDrawable)
                    ?.setColor(color.color(Munch.UseCase.MESSAGES_OUT, Munch.Theme.WHITE))
        } else {

            rlToolbar.setBackgroundColor(backgroundDark)
            rlSampleRoot.setBackgroundColor(backgroundDark)
            rlInputBack.setBackgroundColor(backgroundDarkLighter)

            arrayOf(tvTitle, tvBodyIn, tvBodyOut, etInput).forEach { it.setTextColor(mainTextDark) }
            arrayOf(tvDateIn, tvDateOut).forEach { it.setTextColor(otherTextDark) }
            tvSubtitle.setTextColor(minorTextDark)

            arrayOf(ivKeyboard, ivAttach).forEach { it.paint(color.colorDark(50)) }
            (llMessageIn.background as? GradientDrawable)
                    ?.setColor(messageBackgroundDark)
            (llMessageOut.background as? GradientDrawable)
                    ?.setColor(color.color(Munch.UseCase.MESSAGES_OUT, Munch.Theme.DARK))
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
        val sampleLastSeen = LastSeenUtils.getFull(
                context = context,
                isOnline = false,
                timeStamp = time() - 2147,
                deviceCode = 0,
                withSeconds = showSeconds
        )

        tvBodyIn.text = when {
            useAppleEmojis -> EmojiHelper.getEmojied(context, sampleIn, ignorePref = true)
            else -> sampleIn
        }
        tvBodyOut.text = when {
            useAppleEmojis -> EmojiHelper.getEmojied(context, sampleOut, ignorePref = true)
            else -> sampleOut
        }

        tvBodyIn.setTextSize(TypedValue.COMPLEX_UNIT_SP, stMessageSize.value.toFloat())
        tvBodyOut.setTextSize(TypedValue.COMPLEX_UNIT_SP, stMessageSize.value.toFloat())
        etInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, stMessageSize.value.toFloat() + 2)

        tvDateIn.text = sampleDateIn
        tvDateOut.text = sampleDateOut
        tvSubtitle.text = sampleLastSeen

        rlToolbar.tvTitle.text = getString(R.string.appearance_sample_name)
        etInput.setText(getString(R.string.appearance_sample_input))
        tvTitle.lowerIf(inLower)
        etInput.lowerIf(inLower)
    }

    private fun applyVisibility() {
        val showVoice = switchShowVoice.isChecked
        val showStickers = switchShowStickers.isChecked

        ivKeyboard.setVisible(showStickers)
        ivMic.setVisible(showVoice)
        ivSend.setVisible(!showVoice)
    }

    private fun initViews() {
        isLightBefore = Prefs.isLightTheme
        switchLightTheme.onCheckedListener = CompoundButton.OnCheckedChangeListener { _, b ->
            applyColors()
        }
        switchLightTheme.isChecked = isLightBefore
        if (Prefs.chatBack.isNotEmpty()) {
            updatePhoto(Prefs.chatBack)
        }
        btnGallery.setOnClickListener { openGallery() }
        csThemeColor.setOnClickListener {
            showColorPicker(currentColor) { color ->
                currentColor = color
                applyColors()
            }
        }

        switchChatBack.onCheckedListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
            llCustomBack.setVisible(isChecked)
            if (!isChecked) {
                deletePhoto()
            }
        }
        switchChatBack.isChecked = Prefs.chatBack.isNotBlank()
        llCustomBack.setVisible(switchChatBack.isChecked)
        btnColor.setOnClickListener {
            showColorPicker(currentColor, ::convertColor)
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

        stMessageSize.value = Prefs.messageTextSize
        stMessageSize.onValueChangedListener = { applyTexts() }
    }

    private fun openGallery() {
        permissionHelper.doOrRequest(
                arrayOf(PermissionHelper.READ_STORAGE, PermissionHelper.WRITE_STORAGE),
                R.string.no_access_to_storage,
                R.string.need_access_to_storage
        ) {
            bottomSheetHelper.openBottomSheet(GalleryFragment.newInstance(onlyPhotos = true) {
                bottomSheetHelper.closeBottomSheet()
                if (it.isNotEmpty()) {
                    convertPhoto(it[0].path)
                } else {
                    showError(activity, R.string.error)
                }
            }, getString(R.string.gallery))
        }
    }

    private fun deletePhoto() {
        ivBackground.setImageBitmap(null)
        Prefs.chatBack = ""
    }

    private fun convertPhoto(path: String) {
        val activity = activity ?: return

        val newPath = getCroppedImagePath(activity, path)
        if (newPath != null) {
            Prefs.chatBack = newPath
            updatePhoto(newPath)
        } else {
            showAlert(context, getString(R.string.unable_to_crop))
        }
    }

    private fun convertColor(color: Int) {
        val activity = activity ?: return

        val newPath = createColoredBitmap(activity, color)
        if (newPath != null) {
            Prefs.chatBack = newPath
            updatePhoto(newPath)
        } else {
            showAlert(context, getString(R.string.unable_to_pick_color))
        }
    }

    private fun updatePhoto(path: String) {
        ivBackground.load("file://$path")
    }

    override fun onStop() {
        super.onStop()
        GalleryFragment.clear()

        savePreferences()
    }

    /**
     * for parent activity
     */
    fun hasChanges() = isLightBefore != switchLightTheme.isChecked
            || currentColor != colorBefore

    /**
     * for parent activity
     */
    fun askForRestarting() {
        showConfirm(context, getString(R.string.wanna_change_theme)) { yes ->
            if (yes) {
                Prefs.color = currentColor
                Prefs.colorBetterWithWhite = ColorUtils.isColorBetterWithWhite(currentColor)
                Prefs.isLightTheme = switchLightTheme.isChecked
                savePreferences()
                restartApp(context, getString(R.string.theme_changed))
            } else {
                switchLightTheme.isChecked = isLightBefore
                currentColor = colorBefore
                activity?.onBackPressed()
            }
        }
    }

    private fun showColorPicker(initColor: Int, onPicked: (Int) -> Unit) {
        ColorPickerDialogBuilder.with(context)
                .initialColor(initColor)
                .lightnessSliderOnly()
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setPositiveButton(R.string.ok) { _, color, _ ->
                    onPicked(color)
                }
                .setNegativeButton(R.string.cancel, null)
                .build()
                .apply { stylize() }
                .show()
    }

    private fun savePreferences() {
        Prefs.showSeconds = switchShowSeconds.isChecked
        Prefs.lowerTexts = switchLowerTexts.isChecked
        Prefs.appleEmojis = switchAppleEmojis.isChecked
        Prefs.showStickers = switchShowStickers.isChecked
        Prefs.showVoice = switchShowVoice.isChecked
        Prefs.messageTextSize = stMessageSize.value
    }

    companion object {

        fun newInstance() = AppearanceFragment()
    }
}