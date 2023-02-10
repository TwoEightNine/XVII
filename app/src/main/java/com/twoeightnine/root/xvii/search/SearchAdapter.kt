/*
 * xvii - messenger for vk
 * Copyright (C) 2021  TwoEightNine
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.twoeightnine.root.xvii.search

import android.content.Context
import android.text.Html
import android.text.SpannableStringBuilder
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.extensions.load
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.utils.EmojiHelper
import com.twoeightnine.root.xvii.utils.wrapMentions
import global.msnthrp.xvii.data.dialogs.Dialog
import global.msnthrp.xvii.uikit.base.adapters.BaseAdapter
import global.msnthrp.xvii.uikit.extensions.hide
import global.msnthrp.xvii.uikit.extensions.lowerIf
import kotlinx.android.synthetic.main.item_dialog.view.*
import kotlinx.android.synthetic.main.item_dialog_search.view.*
import kotlinx.android.synthetic.main.item_dialog_search.view.civPhoto
import kotlinx.android.synthetic.main.item_dialog_search.view.ivOnlineDot
import kotlinx.android.synthetic.main.item_dialog_search.view.rlItemContainer
import kotlinx.android.synthetic.main.item_dialog_search.view.tvBody
import kotlinx.android.synthetic.main.item_dialog_search.view.tvTitle

class SearchAdapter(
        context: Context,
        private val onClick: (SearchDialog) -> Unit,
        private val onLongClick: (SearchDialog) -> Unit
) : BaseAdapter<SearchDialog, SearchAdapter.SearchViewHolder>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = SearchViewHolder(inflater.inflate(R.layout.item_dialog_search, null))

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bind(items[position])
    }

    private fun getMessageBody(context: Context, dialog: SearchDialog): String {
        if (dialog.text.isNotEmpty()) {
            return wrapMentions(context, dialog.text).toString()
        }
        return context.getString(R.string.error_message)
    }

    inner class SearchViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(dialog: SearchDialog) {
            with(itemView) {
                civPhoto.load(dialog.photo)
                if (dialog.isChat == true) {
                    tvTitle.text = dialog.title
                    tvTitle.lowerIf(Prefs.lowerTexts)
                    tvBody.text = if (EmojiHelper.hasEmojis(dialog.text)) {
                        EmojiHelper.getEmojied(
                            context,
                            dialog.text,
                            Html.fromHtml(getMessageBody(context, dialog)) as SpannableStringBuilder
                        )
                    } else {
                        Html.fromHtml(getMessageBody(context, dialog))
                    }
                }else{
                    tvTitleSingle.text = dialog.title
                    tvTitleSingle.lowerIf(Prefs.lowerTexts)
                }
                ivOnlineDot.hide() // due to this list is not autorefreshable

                rlItemContainer.setOnClickListener {
                    items.getOrNull(adapterPosition)
                            ?.also(onClick)
                }
                rlItemContainer.setOnLongClickListener {
                    items.getOrNull(adapterPosition)
                            ?.also(onLongClick)
                    true
                }
            }
        }
    }

}