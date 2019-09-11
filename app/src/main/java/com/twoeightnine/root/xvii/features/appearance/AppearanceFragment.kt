package com.twoeightnine.root.xvii.features.appearance

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.chats.attachments.gallery.GalleryFragment
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.utils.*
import com.twoeightnine.root.xvii.views.LoadingDialog
import kotlinx.android.synthetic.main.fragment_appearance.*

class AppearanceFragment : BaseFragment() {

    private lateinit var bottomSheetHelper: BottomSheetHelper
    private lateinit var permissionHelper: PermissionHelper

    private var isNightBefore = false
    private var colorBefore = 0
    private var currentColor = 0

    var dialog: LoadingDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        colorBefore = Prefs.color
        currentColor = colorBefore
        initViews()
        applyColors()
        ivPreview.stylize(ColorManager.MAIN_TAG)
        switchNight.stylize()
        rlHideBottom.stylizeColor()
        bottomSheetHelper = BottomSheetHelper(
                rlBottom,
                rlHideBottom,
                tvTitle,
                R.id.flBottom,
                childFragmentManager,
                resources.getDimensionPixelSize(R.dimen.bottomsheet_height)
        )
        permissionHelper = PermissionHelper(this)
    }

    override fun getLayoutId() = R.layout.fragment_appearance

    private fun applyColors() {
        val colors = ColorManager.getFromMain(currentColor)
        rlDark.setBackgroundColor(colors[0])
        rlMain.setBackgroundColor(colors[1])
        rlLight.setBackgroundColor(colors[2])
        rlExtraLight.setBackgroundColor(colors[3])
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        updateTitle(getString(R.string.appearance))
    }

    private fun initViews() {
        isNightBefore = Prefs.isLightTheme
        switchNight.setOnCheckedChangeListener { _, b ->
            Prefs.isLightTheme = b
            llPicker.setVisible(b)
        }
        llPicker.setVisible(isNightBefore)
        switchNight.isChecked = isNightBefore
        if (Prefs.chatBack.isNotEmpty()) {
            XviiPicasso.get()
                    .load("file://${Prefs.chatBack}")
                    .resize(100, 100)
                    .centerCrop()
                    .into(ivPreview)
        }
        rlChatBack.setOnClickListener { showDialog() }
        picker.addOnColorChangedListener {
            currentColor = picker.selectedColor
            applyColors()
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
        ivPreview.setImageResource(R.drawable.ic_add)
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
                .resize(100, 100)
                .centerCrop()
                .into(ivPreview)
    }

    override fun onStop() {
        super.onStop()
        GalleryFragment.clear()
    }

    /**
     * for parent activity
     */
    fun hasChanges() = isNightBefore != switchNight.isChecked
            || switchNight.isChecked && currentColor != colorBefore

    /**
     * for parent activity
     */
    fun askForRestarting() {
        showConfirm(context, getString(R.string.wanna_change_theme)) { yes ->
            if (yes) {
                Prefs.color = currentColor
                restartApp(context, getString(R.string.theme_changed))
            } else {
                switchNight.isChecked = isNightBefore
                currentColor = colorBefore
                activity?.onBackPressed()
            }
        }
    }

    companion object {

        fun newInstance() = AppearanceFragment()
    }
}