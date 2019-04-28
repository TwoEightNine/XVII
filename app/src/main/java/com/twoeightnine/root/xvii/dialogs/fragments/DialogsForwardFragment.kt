package com.twoeightnine.root.xvii.dialogs.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.dialogs.models.Dialog
import com.twoeightnine.root.xvii.utils.show
import kotlinx.android.synthetic.main.toolbar.*

class DialogsForwardFragment : DialogsFragment() {

    private val forwarded by lazy {
        arguments?.getString(ARG_FORWARDED)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.clear()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        toolbar.show()
        updateTitle(getString(R.string.choose_dialog))
    }

    override fun onClick(dialog: Dialog) {
        val data = Intent().apply {
            putExtra(ARG_DIALOG, dialog)
            putExtra(ARG_FORWARDED, forwarded)
        }
        activity?.apply {
            setResult(Activity.RESULT_OK, data)
            finish()
        }
    }

    override fun onLongClick(dialog: Dialog) {}

    companion object {
        const val ARG_FORWARDED = "forwarded"
        const val ARG_DIALOG = "dialog"

        fun newInstance(forwarded: String): DialogsForwardFragment {
            val fragment = DialogsForwardFragment()
            fragment.arguments = Bundle().apply {
                putString(ARG_FORWARDED, forwarded)
            }
            return fragment
        }
    }
}