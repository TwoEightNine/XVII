package com.twoeightnine.root.xvii.settings.fragments

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import android.view.View
import com.squareup.picasso.Picasso
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.fragments.GalleryFragment
import com.twoeightnine.root.xvii.fragments.BaseFragment
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.utils.*
import com.twoeightnine.root.xvii.views.LoadingDialog
import kotlinx.android.synthetic.main.fragment_appearance.*

class AppearanceFragment : BaseFragment() {

    private lateinit var imut: ImageUtils
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
        imut = ImageUtils(safeActivity)
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
        isNightBefore = Prefs.isNight
        switchNight.setOnCheckedChangeListener { _, b ->
            Prefs.isNight = b
            if (b) {
                llPicker.visibility = View.VISIBLE
            } else {
                llPicker.visibility = View.GONE
            }
        }
        if (isNightBefore) {
            llPicker.visibility = View.VISIBLE
        } else {
            llPicker.visibility = View.GONE
        }
        switchNight.isChecked = isNightBefore
        if (Prefs.chatBack.isNotEmpty()) {
            Picasso.with(context)
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
            bottomSheetHelper.openBottomSheet(GalleryFragment.newInstance({
                bottomSheetHelper.closeBottomSheet()
                if (it.size > 0) {
                    convertPhoto(it[0])
                } else {
                    showError(activity, R.string.error)
                }
            }, true), getString(R.string.gallery))
        }
    }

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
        Picasso.with(App.context)
                .load("file://$newPath")
                .resize(100, 100)
                .centerCrop()
                .into(ivPreview)
    }

    override fun onStop() {
        super.onStop()
        if (isNightBefore != switchNight.isChecked ||
                switchNight.isChecked && currentColor != colorBefore) {
            Prefs.color = currentColor
            restartApp(getString(R.string.theme_changed))
        }
    }
}