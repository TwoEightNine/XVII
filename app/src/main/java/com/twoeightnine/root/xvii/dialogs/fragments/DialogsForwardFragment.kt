package com.twoeightnine.root.xvii.dialogs.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.dialogs.models.Dialog

class DialogsForwardFragment : DialogsFragment() {

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.clear()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        updateTitle(getString(R.string.choose_dialog))
    }

    override fun onClick(dialog: Dialog) {
        val forwarded = arguments?.getString(ARG_FORWARDED) ?: return
//        rootActivity?.onBackPressed()
//        rootActivity?.loadFragment(ChatFragment.newInstance(dialog, forwarded))
    }

    override fun onLongClick(dialog: Dialog) {}

    companion object {
        const val ARG_FORWARDED = "forwarded"

        fun newInstance(forwarded: String): DialogsForwardFragment {
            val fragment = DialogsForwardFragment()
            fragment.arguments = Bundle().apply {
                putString(ARG_FORWARDED, forwarded)
            }
            return fragment
        }
    }
}