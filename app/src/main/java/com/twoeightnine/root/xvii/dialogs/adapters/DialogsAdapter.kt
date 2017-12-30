package com.twoeightnine.root.xvii.dialogs.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.squareup.picasso.Picasso
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.PaginationAdapter
import com.twoeightnine.root.xvii.consts.Api
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.model.Message
import com.twoeightnine.root.xvii.utils.EmojiHelper
import com.twoeightnine.root.xvii.utils.getTime
import de.hdodenhof.circleimageview.CircleImageView

open class DialogsAdapter(context: Context,
                     loader: (Int) -> Unit,
                     protected var clickListener: (Int) -> Unit,
                     protected var longClickListener: (Int) -> Boolean) : PaginationAdapter<Message>(context, loader) {


    override var stubLoadItem: Message? = Message.stubLoad

    override fun isStubLoad(obj: Message) = Message.isStubLoad(obj)

    override var stubTryItem: Message? = Message.stubTry

    override fun isStubTry(obj: Message) = Message.isStubTry(obj)

    override fun createHolder(parent: ViewGroup,
                              viewType: Int) = DialogViewHolder(inflater.inflate(R.layout.item_dialog, parent, false))


    override fun onBindViewHolder(vholder: RecyclerView.ViewHolder, position: Int) {
        val message = items[position]
        val holder: DialogViewHolder
        if (vholder is DialogViewHolder) {
            holder = vholder
        } else {
            return
        }


        val photo = message.photo
        if (photo != null) {
            Picasso.with(App.context)
                    .load(photo)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(holder.civPhoto)
        } else {
            Picasso //fixme what's wrong
                    .with(App.context)
                    .load(Api.PHOTO_STUB)
                    .into(holder.civPhoto)
        }

        holder.tvTitle.text = message.title
        if (message.emoji == 1) {
            holder.tvBody.text = EmojiHelper.getEmojied(
                    context,
                    message.body ?: "",
                    Html.fromHtml(getMessageBody(App.context, message)) as SpannableStringBuilder
            )
            holder.tvBody.maxLines = 1
        } else {
            holder.tvBody.text = Html.fromHtml(getMessageBody(App.context, message))
            holder.tvBody.maxLines = 2
        }
        holder.tvDate.text = getTime(message.date, false)

        if (message.online == 1) {
            holder.ivOnlineDot.visibility = View.VISIBLE
        } else {
            holder.ivOnlineDot.visibility = View.GONE
        }
        holder.rlMute.visibility = if (message.isMute) View.VISIBLE else View.GONE


        if (!message.isRead) {
            if (message.isOut) {
                holder.ivUnreadDotOut.setImageResource(R.drawable.unread_dot_shae)
                holder.rlUnreadCount.visibility = View.GONE
            } else if (message.unread != 0) {
                holder.rlUnreadCount.visibility = View.VISIBLE
                val unread = if (message.unread > 99) context.getString(R.string.unread100) else message.unread.toString()
                holder.tvUnreadCount.text = unread
                holder.ivUnreadDotOut.setImageDrawable(null)
            }
        } else {
            holder.ivUnreadDotOut.setImageDrawable(null)
            holder.rlUnreadCount.visibility = View.GONE
        }

        Style.forImageView(holder.ivOnlineDot, Style.MAIN_TAG)
        Style.forImageView(holder.ivUnreadDotOut, Style.MAIN_TAG)
        Style.forViewGroup(holder.rlUnreadCount)
    }

    private fun getMessageBody(context: Context, message: Message): String {
        val fwdCount = if (message.fwdMessages == null) 0 else (message.fwdMessages as ArrayList).size
        val attCount = if (message.attachments == null) 0 else (message.attachments as ArrayList).size
        if (!TextUtils.isEmpty(message.body))
            return message.body ?: ""
        if (fwdCount != 0)
            return context.getString(R.string.fwd_message, fwdCount)
        if (attCount != 0) {
            return context.resources.getQuantityString(R.plurals.attach, attCount, attCount)
        }
        if (message.action == Message.OUTOFCHAT) {
            return context.getString(R.string.kick_chat)
        }
        if (message.action == Message.INCHAT) {
            return context.getString(R.string.invite_chat)
        }
        return context.getString(R.string.error_message)
    }

    inner class DialogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        @BindView(R.id.civPhoto)
        lateinit var civPhoto: CircleImageView
        @BindView(R.id.rlMute)
        lateinit var rlMute: RelativeLayout
        @BindView(R.id.tvTitle)
        lateinit var tvTitle: TextView
        @BindView(R.id.tvBody)
        lateinit var tvBody: TextView
        @BindView(R.id.tvDate)
        lateinit var tvDate: TextView
        @BindView(R.id.ivOnlineDot)
        lateinit var ivOnlineDot: ImageView
        @BindView(R.id.ivUnreadDotOut)
        lateinit var ivUnreadDotOut: ImageView
        @BindView(R.id.tvUnreadCount)
        lateinit var tvUnreadCount: TextView
        @BindView(R.id.rlUnreadCount)
        lateinit var rlUnreadCount: RelativeLayout
        @BindView(R.id.rlItemContainer)
        lateinit var rlItemContainer: RelativeLayout

        init {
            ButterKnife.bind(this, itemView)
            rlItemContainer.setOnClickListener({ clickListener.invoke(adapterPosition) })
            rlItemContainer.setOnLongClickListener({ longClickListener.invoke(adapterPosition) })
        }
    }


}