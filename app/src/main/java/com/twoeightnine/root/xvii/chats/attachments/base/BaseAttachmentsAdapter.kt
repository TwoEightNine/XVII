package com.twoeightnine.root.xvii.chats.attachments.base

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.base.BaseReachAdapter

abstract class BaseAttachmentsAdapter<T : Any, VH : BaseAttachmentsAdapter.BaseAttachmentViewHolder<T>>(
        context: Context,
        loader: (Int) -> Unit
) : BaseReachAdapter<T, VH>(context, loader) {

    abstract fun getViewHolder(view: View): BaseAttachmentViewHolder<T>

    abstract fun getLayoutId(): Int

    override fun createHolder(parent: ViewGroup, viewType: Int) = getViewHolder(inflater.inflate(getLayoutId(), null))

    override fun bind(holder: VH, item: T) {
        (holder as? BaseAttachmentViewHolder<T>)?.bind(item)
    }

    abstract class BaseAttachmentViewHolder<T>(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(item: T)

    }
}