package com.twoeightnine.root.xvii.chats.adapters.attachments

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import butterknife.BindView
import butterknife.ButterKnife
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.SimpleAdapter
import com.twoeightnine.root.xvii.model.Attachment
import com.twoeightnine.root.xvii.utils.getDoc
import com.twoeightnine.root.xvii.utils.getEncrypted
import com.twoeightnine.root.xvii.utils.getPhoto
import com.twoeightnine.root.xvii.utils.getVideo

class AttachmentsAdapter(private val listener: ((Int) -> Unit)?) : SimpleAdapter<Attachment>() {

    override fun getView(pos: Int, view: View?, viewGroup: ViewGroup): View {
        var view = view
        val att = items[pos]
        if (view == null) {
            view = View.inflate(App.context, R.layout.item_attachment, null)
            view!!.tag = ViewHolder(view)
        }
        val holder = view.tag as ViewHolder
        holder.llContainer.removeAllViews()
        when (att.type) {

            Attachment.TYPE_PHOTO -> {
                val photo = att.photo
                if (photo != null)
                    holder.llContainer.addView(getPhoto(photo, App.context))
            }

            Attachment.TYPE_DOC -> {
                val doc = att.doc
                if (doc != null) {
                    if (doc.isEncrypted) {
                        holder.llContainer.addView(getEncrypted(doc, App.context))
                    } else {
                        holder.llContainer.addView(getDoc(doc, App.context))
                    }
                }
            }

            Attachment.TYPE_VIDEO -> {
                val video = att.video
                if (video != null)
                    holder.llContainer.addView(getVideo(video, App.context))
            }
        }
        holder.rlClose.setOnClickListener { listener?.invoke(pos) }
        return view
    }

    inner class ViewHolder(view: View) {

        @BindView(R.id.llContainer)
        lateinit var llContainer: LinearLayout
        @BindView(R.id.rlClose)
        lateinit var rlClose: RelativeLayout

        init {
            ButterKnife.bind(this, view)
        }
    }
}