package com.twoeightnine.root.xvii.dialogs.adapters

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.PaginationAdapter
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.model.Message
import com.twoeightnine.root.xvii.utils.loadPhoto
import de.hdodenhof.circleimageview.CircleImageView

class SearchMessagesAdapter(context: Context,
                            loader: (Int) -> Unit,
                            private var listener: (Int) -> Unit,
                            private var longListener: (Int) -> Boolean) : PaginationAdapter<Message>(context, loader) {

    override fun createHolder(parent: ViewGroup, viewType: Int): SearchDialogViewHolder {
        return SearchDialogViewHolder(View.inflate(context, R.layout.item_dialog_search, null))
    }

    override fun onBindViewHolder(vholder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        val message = items[position]
        val holder: SearchDialogViewHolder
        if (vholder is SearchDialogViewHolder) {
            holder = vholder
        } else {
            return
        }

        val photo = message.photo ?: "http://www.iconsdb.com/icons/preview/light-gray/square-xxl.png"
        holder.civPhoto.loadPhoto(photo)
        holder.tvTitle.text = message.title

        if (message.online == 1) {
            holder.ivOnlineDot.visibility = View.VISIBLE
        } else {
            holder.ivOnlineDot.visibility = View.GONE
        }

        Style.forImageView(holder.ivOnlineDot, Style.MAIN_TAG)

    }

    override var stubLoadItem: Message? = Message.stubLoad

    override fun isStubLoad(obj: Message) = Message.isStubLoad(obj)

    override var stubTryItem: Message? = Message.stubTry

    override fun isStubTry(obj: Message) = Message.isStubTry(obj)

    inner class SearchDialogViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {

        @BindView(R.id.civPhoto)
        lateinit var civPhoto: CircleImageView
        @BindView(R.id.tvTitle)
        lateinit var tvTitle: TextView
        @BindView(R.id.ivOnlineDot)
        lateinit var ivOnlineDot: ImageView
        @BindView(R.id.rlItemContainer)
        lateinit var rlItemContainer: RelativeLayout

        init {
            ButterKnife.bind(this, itemView)
            rlItemContainer.setOnClickListener({ listener.invoke(adapterPosition) })
            rlItemContainer.setOnLongClickListener({ longListener.invoke(adapterPosition) })
        }

    }
}