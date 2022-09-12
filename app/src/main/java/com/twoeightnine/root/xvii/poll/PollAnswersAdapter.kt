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

package com.twoeightnine.root.xvii.poll

import android.content.Context
import android.os.Build
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseMultiSelectAdapter
import com.twoeightnine.root.xvii.model.attachments.PollAnswer
import com.twoeightnine.root.xvii.uikit.Munch
import com.twoeightnine.root.xvii.uikit.paint
import global.msnthrp.xvii.uikit.extensions.setVisibleWithInvis
import global.msnthrp.xvii.uikit.extensions.show
import kotlinx.android.synthetic.main.item_poll_answer.view.*

class PollAnswersAdapter(
        context: Context,
        private val multiple: Boolean,
        private val voted: Boolean
) : BaseMultiSelectAdapter<PollAnswer, PollAnswersAdapter.PollAnswerViewHolder>(context) {

    private var ignore: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            PollAnswerViewHolder(inflater.inflate(R.layout.item_poll_answer, parent, false))

    override fun onBindViewHolder(holder: PollAnswerViewHolder, position: Int) {
        holder.bind(items[position])
    }

    fun invalidateSelected(answerIds: List<Long>) {
        items.filter { it.id in answerIds }
                .forEach { item ->
                    multiSelect(item)
                    ignore = true
                }
        notifyDataSetChanged()
    }

    inner class PollAnswerViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(pollAnswer: PollAnswer) {
            with(itemView) {
                if (voted) {
                    pbRating.progressDrawable.paint(Munch.color.color)
                    if (Build.VERSION.SDK_INT >= 24) {
                        pbRating.setProgress(pollAnswer.rate.toInt(), true)
                    } else {
                        pbRating.progress = pollAnswer.rate.toInt()
                    }
                    pbRating.show()
                    tvPercentage.show()
                    tvPercentage.text = "${pollAnswer.rate}%"
                }
                tvAnswer.text = pollAnswer.text
                ivCheck.paint(Munch.color.color)
                ivCheck.setVisibleWithInvis(pollAnswer in multiSelect)
                setOnClickListener {
                    if (ignore) return@setOnClickListener

                    val item = items[adapterPosition]
                    if (!multiple) {
                        clearMultiSelect()
                    }
                    multiSelect(item)
                    ivCheck.setVisibleWithInvis(item in multiSelect)
                    notifyDataSetChanged()
                }
            }
        }

    }
}