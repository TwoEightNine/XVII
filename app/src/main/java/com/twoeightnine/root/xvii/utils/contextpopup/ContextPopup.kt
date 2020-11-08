package com.twoeightnine.root.xvii.utils.contextpopup

import android.content.Context
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.utils.stylize

fun createContextPopup(context: Context, items: List<ContextPopupItem>): AlertDialog =

        AlertDialog.Builder(context).create().apply {
            val itemHeight = context.resources.getDimensionPixelSize(R.dimen.context_popup_item_height)
            val adapter = ContextPopupAdapter(context, this).apply {
                addAll(items.toMutableList())
            }
            RecyclerView(context).apply {
                layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT, itemHeight * items.size)
                layoutManager = LinearLayoutManager(context)
                this.adapter = adapter
                setView(this)
            }
            stylize()
        }
