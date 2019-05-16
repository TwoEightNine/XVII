package com.twoeightnine.root.xvii.chats.attachments.attached

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.BaseAdapter
import com.twoeightnine.root.xvii.model.attachments.Attachment
import com.twoeightnine.root.xvii.utils.load
import com.twoeightnine.root.xvii.utils.setVisible
import com.twoeightnine.root.xvii.utils.showAlert
import com.twoeightnine.root.xvii.utils.stylize
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
        get() = try {
            Integer.parseInt(fwdMessages)
        } catch (e: java.lang.Exception) {
            null
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = AttachmentViewHolder(inflater.inflate(R.layout.item_attached, null))

    override fun onBindViewHolder(holder: AttachmentViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun add(item: Attachment) {
        if (count < 10) {
            super.add(item)
            updateCounter()
        } else {
            showAlert(context, context.getString(R.string.ten_attachments))
        }
    }

    override fun addAll(items: MutableList<Attachment>, pos: Int) {
        when {
            count >= 10 -> showAlert(context, context.getString(R.string.ten_attachments))
            count + items.size > 10 -> {
                showAlert(context, context.getString(R.string.ten_attachments))
                super.addAll(items.subList(0, 10 - count), pos)
            }
            else -> super.addAll(items, pos)
        }
        updateCounter()
    }

    override fun clear() {
        fwdMessages = ""
        super.clear()
        updateCounter()
    }

    fun asString() = items
            .filterNot { it === STUB_FWD_MESSAGES }
            .map { it.toString() }
            .joinToString(separator = ",")

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
                val attachPhoto = attachment.photo?.getSmallPhoto()?.url?.let { it }
                        ?: attachment.video?.photo130?.let { it }

                rlFwdMessages.setVisible(isForwarded)
                fwdMessages?.apply {
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
                setOnLongClickListener {
                    if (isForwarded) {
                        fwdMessages = ""
                    } else {
                        remove(attachment)
                    }
                    updateCounter()
                    true
                }
            }
        }
    }
}