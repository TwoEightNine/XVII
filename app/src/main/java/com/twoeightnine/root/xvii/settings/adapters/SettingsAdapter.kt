package com.twoeightnine.root.xvii.settings.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.SimpleAdapter
import com.twoeightnine.root.xvii.settings.Item

class SettingsAdapter(context: Context) : SimpleAdapter<Item>() {

    private val inflater = LayoutInflater.from(context)

    override fun getView(pos: Int, v: View?, viewGroup: ViewGroup): View {
        var view = v
        val item = items[pos]
        if (view == null) {
            view = inflater.inflate(R.layout.item_settings, null)
            view!!.tag = ViewHolder(view)
        }
        val holder = view.tag as ViewHolder
        holder.tvTitle.text = item.title
        return view
    }

    inner class ViewHolder(view: View) {

        @BindView(R.id.tvTitle)
        lateinit var tvTitle: TextView

        init {
            ButterKnife.bind(this, view)
        }
    }
}

