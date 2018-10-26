package com.twoeightnine.root.xvii.settings.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.*
import butterknife.BindView
import butterknife.ButterKnife
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.slider.LightnessSlider
import com.squareup.picasso.Picasso
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.fragments.GalleryFragment
import com.twoeightnine.root.xvii.fragments.BaseFragment
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.utils.*
import com.twoeightnine.root.xvii.views.LoadingDialog

class AppearanceFragment : BaseFragment() {

    private val requestPermission = 420

    @BindView(R.id.rlChatBack)
    lateinit var rlChatBack: RelativeLayout
    @BindView(R.id.ivPreview)
    lateinit var ivPreview: ImageView
    @BindView(R.id.switchNight)
    lateinit var night: Switch
    @BindView(R.id.llPicker)
    lateinit var llPicker: LinearLayout
    @BindView(R.id.rlDark)
    lateinit var rlDark: RelativeLayout
    @BindView(R.id.rlMain)
    lateinit var rlMain: RelativeLayout
    @BindView(R.id.rlLight)
    lateinit var rlLight: RelativeLayout
    @BindView(R.id.rlExtraLight)
    lateinit var rlExtraLight: RelativeLayout
    @BindView(R.id.rlBottom)
    lateinit var rlBottom: RelativeLayout
    @BindView(R.id.rlHideBottom)
    lateinit var rlHideBottom: RelativeLayout
    @BindView(R.id.tvTitle)
    lateinit var tvTitle: TextView

    @BindView(R.id.picker)
    lateinit var picker: ColorPickerView
    @BindView(R.id.pickerLight)
    lateinit var lightness: LightnessSlider

    private lateinit var imut: ImageUtils
    private lateinit var bottomSheetHelper: BottomSheetHelper
    private lateinit var permissionHelper: PermissionHelper

    private var isNightBefore: Boolean = false
    private var colorBefore: Int = 0
    private var currentColor: Int = 0

    var dialog: LoadingDialog? = null

    override fun bindViews(view: View) {
        super.bindViews(view)
        ButterKnife.bind(this, view)
        colorBefore = Prefs.color
        currentColor = colorBefore
        initViews()
        applyColors()
        imut = ImageUtils(activity)
        Style.forImageView(ivPreview, Style.MAIN_TAG)
        Style.forSwitch(night)
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
        night.setOnCheckedChangeListener { _, b ->
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
        night.isChecked = isNightBefore
        if (Prefs.chatBack.isNotEmpty()) {
            Picasso.with(context)
                    .load("file://${Prefs.chatBack}")
                    .resize(100, 100)
                    .centerCrop()
                    .into(ivPreview)
        }
        rlChatBack.setOnClickListener { openGallery() }
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
            getFromGallery()
        }
    }

    private fun getFromGallery() {
        if (hasPermissions()) {
            bottomSheetHelper.openBottomSheet(GalleryFragment.newInstance({
                bottomSheetHelper.closeBottomSheet()
                if (it.size > 0) {
                    convertPhoto(it[0])
                } else {
                    showError(activity, R.string.error)
                }
            }, true), getString(R.string.gallery))
        } else {
            val dialog = AlertDialog.Builder(activity)
                    .setMessage(R.string.permissions_info)
                    .setPositiveButton(android.R.string.ok, {
                        _, _ ->
                        requestPermissions(arrayOf(
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                requestPermission)
                    })
                    .create()
            dialog.show()
            Style.forDialog(dialog)

        }
    }

    private fun showDialog() {
        if (Prefs.chatBack.isNotEmpty()) {
            val dialog = AlertDialog.Builder(context)
                    .setMessage(R.string.chat_back_exists)
                    .setPositiveButton(R.string.change) { _, _ -> getFromGallery() }
                    .setNegativeButton(R.string.delete) { _, _ -> deletePhoto() }
                    .create()
            dialog.show()
            Style.forDialog(dialog)
        } else {
            getFromGallery()
        }
    }

    private fun deletePhoto() {
        ivPreview.setImageResource(R.drawable.ic_add)
        Prefs.chatBack = ""
    }

    private fun convertPhoto(path: String) {
        val newPath = getCroppedImagePath(activity, path)
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
        if (isNightBefore != night.isChecked ||
                night.isChecked && currentColor != colorBefore) {
            Prefs.color = currentColor
            restartApp(getString(R.string.theme_changed))
        }
    }

    private fun hasPermissions() = Build.VERSION.SDK_INT < 23 ||
            ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED &&

                    ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_GRANTED
}