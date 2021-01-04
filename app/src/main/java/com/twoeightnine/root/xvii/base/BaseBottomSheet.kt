package com.twoeightnine.root.xvii.base

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.twoeightnine.root.xvii.R

open class BaseBottomSheet : BottomSheetDialogFragment() {

    override fun getTheme(): Int = R.style.BottomSheetDialog

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        BottomSheetDialog(requireContext(), theme)

    fun show(fragmentManager: FragmentManager) {
        if (!isAdded) {
            show(fragmentManager, tag)
        }
    }
}