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
import kotlinx.android.synthetic.main.item_attached.view.*

class AttachedAdapter(
        context: Context,
        private val onClick: (Attachment) -> Unit
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
        }

    val count
        get() = itemCount

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
            = AttachmentViewHolder(inflater.inflate(R.layout.item_attached, null))

    override fun onBindViewHolder(holder: AttachmentViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun addAll(items: MutableList<Attachment>, pos: Int) {
        if (count + items.size > 10) {
            showAlert(context, context.getString(R.string.ten_attachments))
            super.addAll(items.subList(0, 10 - count), pos)
        } else {
            super.addAll(items, pos)
        }
    }

    override fun clear() {
        fwdMessages = ""
        super.clear()
    }

    fun asString() = items
            .filterNot { it === STUB_FWD_MESSAGES }
            .map { it.toString() }
            .joinToString(separator = ",")

    companion object {
        private val STUB_FWD_MESSAGES = Attachment()
    }

    inner class AttachmentViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(attachment: Attachment) {
            with(itemView) {
                val isFwd = attachment === STUB_FWD_MESSAGES
                val isEncrypted = attachment.doc?.isEncrypted == true
                ivReply.setVisible(isFwd)
                ivEncrypted.setVisible(isEncrypted)
                ivAttach.setVisible(attachment.photo != null)
                when (attachment.type) {
                    Attachment.TYPE_PHOTO -> attachment.photo?.photo130?.let {
                        ivAttach.load(it)
                        tvInfo.text = ""
                    }
                    Attachment.TYPE_VIDEO -> attachment.video?.photo130?.let {
                        ivAttach.load(it)
                        tvInfo.text = ""
                    }
                    Attachment.TYPE_DOC -> attachment.doc?.let {
                        if (!it.isEncrypted) {
                            tvInfo.text = it.ext
                        }
                    }
                    else -> attachment.type?.let { tvInfo.text = it }
                }
                setOnClickListener { onClick(items[adapterPosition]) }
                setOnLongClickListener {
                    if (isFwd) {
                        fwdMessages = ""
                    } else {
                        remove(attachment)
                    }
                    true
                }
            }
        }
    }
}