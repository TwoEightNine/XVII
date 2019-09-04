package com.twoeightnine.root.xvii.utils.contextpopup

import android.content.Context
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.utils.stylize

fun createContextPopup(context: Context, items: List<ContextPopupItem>, title: String = ""): AlertDialog {
    val itemHeight = context.resources.getDimensionPixelSize(R.dimen.context_popup_item_height)
    val content = RecyclerView(context)
    content.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, itemHeight * items.size)
    val dialog = AlertDialog.Builder(context).create()

    val adapter = ContextPopupAdapter(context, dialog)
    content.layoutManager = LinearLayoutManager(context)
    content.adapter = adapter
    adapter.addAll(items.toMutableList())

    dialog.setView(content)
    dialog.stylize()
    return dialog
}