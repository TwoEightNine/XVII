package com.twoeightnine.root.xvii.utils

import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.uikit.XviiToolbar

class AppBarLifter(private val xviiToolbar: XviiToolbar) : RecyclerView.OnScrollListener() {

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        xviiToolbar.isLifted = recyclerView.canScrollVertically(-1)
    }
}