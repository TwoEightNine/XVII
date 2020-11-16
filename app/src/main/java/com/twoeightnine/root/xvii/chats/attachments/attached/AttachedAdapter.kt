package com.twoeightnine.root.xvii.chats.attachments.attached

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.extensions.load
import com.twoeightnine.root.xvii.model.attachments.Attachment
import com.twoeightnine.root.xvii.utils.showAlert
import com.twoeightnine.root.xvii.utils.stylize
import global.msnthrp.xvii.uikit.base.adapters.BaseAdapter
import global.msnthrp.xvii.uikit.extensions.setVisible
import kotlinx.android.synthetic.main.item_attached.view.*

class AttachedAdapter(
        context: Context,
        private val onClick: (Attachment) -> Unit,
        private val onCounterUpdated: (Int) -> Unit
) : BaseAdapter<Attachment, AttachedAdapter.AttachmentViewHolder>(context) {

    var fwdMessages = ""
        set(value) {
            if (field.isEmpty() && value.isNotEmpty()) {
                items.add(0, STUB_FWD_MESSAGES)
            } else if (field.isNotEmpty() && value.isEmpty()) {
                items.remove(STUB_FWD_MESSAGES)
            }
            notifyDataSetChanged()
            field = value
            isReply = false
            updateCounter()
        }

    /**
     * indicates if [fwdMessages] is replied message and should be handled differently
     * should be set after every change of [fwdMessages]
     */
    var isReply = false

    val count
        get() = itemCount

    val replyTo: Int?
        get() = if (isReply) {
            try {
                Integer.parseInt(fwdMessages)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }

    val maxOrder: Int
        get() = attachmentsOrder.maxOrNull() ?: 0

    private val attachmentsOrder = arrayListOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            AttachmentViewHolder(inflater.inflate(R.layout.item_attached, parent, false))

    override fun onBindViewHolder(holder: AttachmentViewHolder, position: Int) {
        holder.bind(items[position])
    }

    fun addWithOrder(item: Attachment, order: Int) {
        if (count < 10) {
            val orderPos = attachmentsOrder.filter { it <= order }.count()
            val delta = if (fwdMessages.isNotBlank()) 1 else 0
            attachmentsOrder.add(orderPos, order)
            super.add(item, orderPos + delta)
            updateCounter()
        } else {
            showAlert(context, context.getString(R.string.ten_attachments))
        }
    }

    override fun clear() {
        fwdMessages = ""
        super.clear()
        attachmentsOrder.clear()
        updateCounter()
    }

    fun asString() = items
            .filterNot { it === STUB_FWD_MESSAGES }
            .joinToString(separator = ",") { it.toString() }

    private fun updateCounter() {
        onCounterUpdated(count)
    }

    companion object {
        private val STUB_FWD_MESSAGES = Attachment()
    }

    inner class AttachmentViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(attachment: Attachment) {
            with(itemView) {
                cvItem.stylize()
                val isForwarded = attachment == STUB_FWD_MESSAGES
                val isEncrypted = attachment.doc?.isEncrypted == true
                val infoText = when {
                    isEncrypted -> null
                    attachment.doc != null && !isEncrypted -> attachment.doc?.ext
                    else -> attachment.type
                }
                val attachPhoto = attachment.photo?.getSmallPhoto()?.url
                        ?: attachment.video?.photo130

                llFwdMessages.setVisible(isForwarded)
                fwdMessages.apply {
                    if (isForwarded) {
                        tvFwdCount.text = "${split(",").size}"
                    }
                }
                tvInfo.setVisible(infoText != null)
                tvInfo.text = infoText

                ivEncrypted.setVisible(isEncrypted)
                ivAttach.setVisible(attachPhoto != null)
                ivAttach.load(attachPhoto)

                setOnClickListener { onClick(items[adapterPosition]) }
                ivClear.setOnClickListener {
                    if (isForwarded) {
                        fwdMessages = ""
                    } else {
                        val index = remove(attachment)
                        val delta = if (fwdMessages.isNotBlank()) -1 else 0
                        attachmentsOrder.removeAt(index + delta)
                    }
                    updateCounter()
                }
            }
        }
    }
}