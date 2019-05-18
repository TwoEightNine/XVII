package com.twoeightnine.root.xvii.utils

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import com.twoeightnine.root.xvii.R
import kotlinx.android.synthetic.main.item_context_popup.view.*


data class ContextPopupItem(
        val iconRes: Int,
        val textRes: Int,
        val onClick: () -> Unit
)

fun createContextPopup(context: Context, items: List<ContextPopupItem>, title: String = ""): AlertDialog {
    val content = LinearLayout(context)
    content.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    val dialog = AlertDialog.Builder(context).create()

    for (item in items) {
        val view = View.inflate(context, R.layout.item_context_popup, null)
        with(view) {
            tvTitle.text = context.getString(item.textRes)
            ivIcon.setImageResource(item.iconRes)
            ivIcon.stylize()
            setOnClickListener {
                dialog.dismiss()
                item.onClick()
            }
        }
        content.addView(view)
    }

    dialog.setView(content)
    dialog.stylize()
    return dialog
}