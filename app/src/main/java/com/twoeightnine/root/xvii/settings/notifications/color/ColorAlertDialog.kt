package com.twoeightnine.root.xvii.settings.notifications.color

import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.managers.Style
import kotlinx.android.synthetic.main.dialog_color.view.*

class ColorAlertDialog(
        context: Context,
        private val onColorSelected: (Int) -> Unit
) : AlertDialog(context) {

    private val adapter by lazy {
        ColorAdapter(context, ::onClick)
    }

    init {
        val view = View.inflate(context, R.layout.dialog_color, null)
        initRecycler(view.rvColor)
        setView(view)
    }

    override fun show() {
        super.show()
        Style.forDialog(this)
    }

    private fun onClick(color: Color) {
        onColorSelected(color.color)
        dismiss()
    }

    private fun initRecycler(recyclerView: RecyclerView) {
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        with(adapter) {
            add(Color(android.graphics.Color.BLACK, R.string.no_color))
            add(Color(android.graphics.Color.RED, R.string.red))
            add(Color(android.graphics.Color.GREEN, R.string.green))
            add(Color(android.graphics.Color.BLUE, R.string.blue))
            add(Color(android.graphics.Color.CYAN, R.string.cyan))
            add(Color(android.graphics.Color.MAGENTA, R.string.magenta))
            add(Color(android.graphics.Color.YELLOW, R.string.yellow))
            add(Color(android.graphics.Color.WHITE, R.string.white))
        }
    }
}