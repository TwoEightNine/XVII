package com.twoeightnine.root.xvii.base

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.twoeightnine.root.xvii.R

abstract class BaseBottomSheet : BottomSheetDialogFragment() {

    abstract fun getLayout(): Int

    override fun getTheme(): Int = R.style.BottomSheetDialog

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        BottomSheetDialog(requireContext(), theme)

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(getLayout(), null)

    fun show(fragmentManager: FragmentManager) {
        if (!isAdded) {
            show(fragmentManager, tag)
        }
    }
}