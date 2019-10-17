package com.twoeightnine.root.xvii.poll

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseMultiSelectAdapter
import com.twoeightnine.root.xvii.model.attachments.PollAnswer
import com.twoeightnine.root.xvii.utils.ColorManager
import com.twoeightnine.root.xvii.utils.setVisibleWithInvis
import com.twoeightnine.root.xvii.utils.stylize
import kotlinx.android.synthetic.main.item_poll_answer.view.*

class PollAnswersAdapter(
        context: Context,
        private val multiple: Boolean
) : BaseMultiSelectAdapter<PollAnswer, PollAnswersAdapter.PollAnswerViewHolder>(context) {

    private var ignore: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            PollAnswerViewHolder(inflater.inflate(R.layout.item_poll_answer, parent, false))

    override fun onBindViewHolder(holder: PollAnswerViewHolder, position: Int) {
        holder.bind(items[position])
    }

    fun invalidateSelected(answerIds: List<Int>) {
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
                tvAnswer.text = pollAnswer.text
                ivCheck.stylize(tag = ColorManager.MAIN_TAG, changeStroke = false)
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