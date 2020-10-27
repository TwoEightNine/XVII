package com.twoeightnine.root.xvii.poll

import android.content.Context
import android.graphics.PorterDuff
import android.os.Build
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseMultiSelectAdapter
import com.twoeightnine.root.xvii.model.attachments.PollAnswer
import com.twoeightnine.root.xvii.uikit.Munch
import com.twoeightnine.root.xvii.uikit.paint
import com.twoeightnine.root.xvii.utils.ColorManager
import com.twoeightnine.root.xvii.utils.setVisibleWithInvis
import com.twoeightnine.root.xvii.utils.show
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
                if (voted) {
                    pbRating.progressDrawable.setColorFilter(ColorManager.mainColor, PorterDuff.Mode.MULTIPLY)
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