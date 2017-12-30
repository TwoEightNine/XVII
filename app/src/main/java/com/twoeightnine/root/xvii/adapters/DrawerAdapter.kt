package com.twoeightnine.root.xvii.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.model.DrawerItem

class DrawerAdapter : SimpleAdapter<DrawerItem>() {

    override fun getView(pos: Int, v: View?, vg: ViewGroup?): View? {
        var view = v
        val item = items[pos]
        if (view == null) {
            view = View.inflate(App.context, R.layout.item_drawer, null)
            view.tag = DrawerViewHolder(view)
        }
        val holder = view?.tag as DrawerViewHolder
        holder.tvDrawer.text = item.title
        holder.ivDrawer.setImageResource(item.resId)
        return view
    }

    inner class DrawerViewHolder(view: View) {
        @BindView(R.id.ivDrawer)
        lateinit var ivDrawer: ImageView
        @BindView(R.id.tvDrawer)
        lateinit var tvDrawer: TextView

        init {
            ButterKnife.bind(this, view)
        }
    }
}