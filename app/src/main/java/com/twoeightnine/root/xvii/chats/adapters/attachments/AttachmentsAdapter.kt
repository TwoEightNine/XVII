package com.twoeightnine.root.xvii.chats.adapters.attachments

import android.view.View
import android.view.ViewGroup
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.SimpleAdapter
import com.twoeightnine.root.xvii.model.Attachment
import com.twoeightnine.root.xvii.model.Photo
import com.twoeightnine.root.xvii.model.Video
import com.twoeightnine.root.xvii.utils.getDoc
import com.twoeightnine.root.xvii.utils.getEncrypted
import com.twoeightnine.root.xvii.utils.getPhoto
import com.twoeightnine.root.xvii.utils.getVideo
import kotlinx.android.synthetic.main.item_attachment.view.*

class AttachmentsAdapter(private val listener: ((Int) -> Unit)?,
                         private val onPhotoClick: (Photo) -> Unit = {},
                         private val onVideoClick: (Video) -> Unit = {}) : SimpleAdapter<Attachment>() {

    override fun getView(pos: Int, view: View?, viewGroup: ViewGroup): View {
        var view = view
        val att = items[pos]
        if (view == null) {
            view = View.inflate(App.context, R.layout.item_attachment, null)
            view!!.tag = ViewHolder(view)
        }
        val holder = view.tag as ViewHolder
        holder.bind(att, pos)
        return view
    }

    inner class ViewHolder(private val view: View) {

        fun bind(attachment: Attachment, pos: Int) {
            with(view) {
                llContainer.removeAllViews()
                when (attachment.type) {

                    Attachment.TYPE_PHOTO -> {
                        val photo = attachment.photo
                        if (photo != null)
                            llContainer.addView(getPhoto(photo, App.context, onPhotoClick))
                    }

                    Attachment.TYPE_DOC -> {
                        val doc = attachment.doc
                        if (doc != null) {
                            if (doc.isEncrypted) {
                                llContainer.addView(getEncrypted(doc, App.context))
                            } else {
                                llContainer.addView(getDoc(doc, App.context))
                            }
                        }
                    }

                    Attachment.TYPE_VIDEO -> {
                        val video = attachment.video
                        if (video != null)
                            llContainer.addView(getVideo(video, App.context, onVideoClick))
                    }
                }
                rlClose.setOnClickListener { listener?.invoke(pos) }
            }
        }
    }
}