package com.twoeightnine.root.xvii.search

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.dialogs.models.Dialog
import com.twoeightnine.root.xvii.extensions.load
import com.twoeightnine.root.xvii.managers.Prefs
import global.msnthrp.xvii.uikit.base.adapters.BaseAdapter
import global.msnthrp.xvii.uikit.extensions.hide
import global.msnthrp.xvii.uikit.extensions.lowerIf
import kotlinx.android.synthetic.main.item_dialog_search.view.*

class SearchAdapter(
        context: Context,
        private val onClick: (Dialog) -> Unit
) : BaseAdapter<Dialog, SearchAdapter.SearchViewHolder>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
            = SearchViewHolder(inflater.inflate(R.layout.item_dialog_search, null))

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class SearchViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(dialog: Dialog) {
            with(itemView) {
                civPhoto.load(dialog.photo)
                tvTitle.text = dialog.title
                tvTitle.lowerIf(Prefs.lowerTexts)
                ivOnlineDot.hide() // due to this list is not autorefreshable
                rlItemContainer.setOnClickListener { onClick(items[adapterPosition]) }
            }
        }
    }
}