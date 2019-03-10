package com.twoeightnine.root.xvii.adapters

import android.view.View
import android.view.ViewGroup
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.model.DrawerItem
import kotlinx.android.synthetic.main.item_drawer.view.*

class DrawerAdapter : SimpleAdapter<DrawerItem>() {

    override fun getView(pos: Int, v: View?, vg: ViewGroup?): View? {
        var view = v
        val item = items[pos]
        if (view == null) {
            view = View.inflate(App.context, R.layout.item_drawer, null)
            view.tag = DrawerViewHolder(view)
        }
        val holder = view?.tag as DrawerViewHolder
        holder.bind(item)
        return view
    }

    inner class DrawerViewHolder(private val view: View) {

        fun bind(drawerItem: DrawerItem) {
            with(view) {
                tvDrawer.text = drawerItem.title
                ivDrawer.setImageResource(drawerItem.resId)
            }
        }
    }
}