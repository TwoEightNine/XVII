package com.twoeightnine.root.xvii.features.appearance

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.squareup.picasso.Picasso
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.attachments.gallery.GalleryFragment
import com.twoeightnine.root.xvii.fragments.BaseOldFragment
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.utils.*
import com.twoeightnine.root.xvii.views.LoadingDialog
import kotlinx.android.synthetic.main.fragment_appearance.*

class AppearanceFragment : BaseOldFragment() {

    private lateinit var bottomSheetHelper: BottomSheetHelper
    private lateinit var permissionHelper: PermissionHelper

    private var isNightBefore: Boolean = false
    private var colorBefore: Int = 0
    private var currentColor: Int = 0

    var dialog: LoadingDialog? = null

    override fun bindViews(view: View) {
        super.bindViews(view)
        colorBefore = Prefs.color
        currentColor = colorBefore
        initViews()
        applyColors()
        Style.forImageView(ivPreview, Style.MAIN_TAG)
        Style.forSwitch(switchNight)
        Style.forViewGroupColor(rlHideBottom)
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

    override fun getLayout() = R.layout.fragment_appearance

    private fun applyColors() {
        val colors = Style.getFromMain(currentColor)
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
            Picasso.get()
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
                    convertPhoto(it[0])
                } else {
                    showError(activity, R.string.error)
                }
            }, getString(R.string.gallery))
        }
    }

    override fun getHomeAsUpIcon() = R.drawable.ic_back

    private fun showDialog() {
        if (Prefs.chatBack.isNotEmpty()) {
            val dialog = AlertDialog.Builder(safeActivity)
                    .setMessage(R.string.chat_back_exists)
                    .setPositiveButton(R.string.change) { _, _ -> openGallery() }
                    .setNegativeButton(R.string.delete) { _, _ -> deletePhoto() }
                    .create()
            dialog.show()
            Style.forDialog(dialog)
        } else {
            openGallery()
        }
    }

    private fun deletePhoto() {
        ivPreview.setImageResource(R.drawable.ic_add)
        Prefs.chatBack = ""
    }

    private fun convertPhoto(path: String) {
        val newPath = getCroppedImagePath(safeActivity, path)
        Prefs.chatBack = newPath
        hideDialog(newPath)
    }

    private fun hideDialog(newPath: String) {
        Picasso.get()
                .load("file://$newPath")
                .resize(100, 100)
                .centerCrop()
                .into(ivPreview)
    }

    override fun onStop() {
        super.onStop()
        GalleryFragment.clear()
        if (isNightBefore != switchNight.isChecked ||
                switchNight.isChecked && currentColor != colorBefore) {
            Prefs.color = currentColor
            restartApp(getString(R.string.theme_changed))
        }
    }

    companion object {

        fun newInstance() = AppearanceFragment()
    }
}