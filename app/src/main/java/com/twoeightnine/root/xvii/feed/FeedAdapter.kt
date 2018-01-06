package com.twoeightnine.root.xvii.feed

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.squareup.picasso.Picasso
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.PaginationAdapter
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.model.Photo
import com.twoeightnine.root.xvii.model.WallPost
import com.twoeightnine.root.xvii.utils.getTime
import com.twoeightnine.root.xvii.utils.loadPhoto
import com.twoeightnine.root.xvii.utils.shortifyNumber
import com.twoeightnine.root.xvii.views.photoviewer.ImageViewerActivity
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

/**
 * Created by root on 12/31/16.
 */

class FeedAdapter(context: Context,
                  loader: (Int) -> Unit,
                  private var onClick: ((Int) -> Unit)?,
                  private var onLike: ((Int) -> Unit)?,
                  private val screenWidth: Int) : PaginationAdapter<WallPost>(context, loader) {

    var nextFrom = ""

    override fun createHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return FeedViewHolder(View.inflate(context, R.layout.item_wall_preview, null))
    }

    override var stubLoadItem: WallPost? = WallPost.stubLoad

    override fun isStubLoad(obj: WallPost) = WallPost.isStubLoad(obj)

    override var stubTryItem: WallPost? = WallPost.stubTry

    override fun isStubTry(obj: WallPost) = WallPost.isStubTry(obj)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val wp = items[position]
        val vholder: FeedViewHolder
        if (holder is FeedViewHolder) {
            vholder = holder
        } else {
            return
        }
        //act
        if (!TextUtils.isEmpty(wp.text)) {
            vholder.tvPost.visibility = View.VISIBLE
            vholder.tvPost.text = getTextPreview(wp.text ?: "")
        } else {
            vholder.tvPost.visibility = View.GONE
        }
        vholder.rlHeader.setOnClickListener { onClick?.invoke(position) }
        vholder.tvPost.setOnClickListener { onClick?.invoke(position) }
        vholder.tvDate.text = getTime(wp.date, true)

        if (wp.group == null && wp.profile == null) {
            return
        }
        val urls = getUrls(wp.photoAttachments)
        vholder.civAvatar.loadPhoto(wp.group?.photo100 ?: wp.profile?.photo100)

        vholder.tvTitle.text = wp.group?.name ?: wp.profile?.fullName()
        if (wp.photoAttachments.size > 0) {

            vholder.ivPreview.visibility = View.VISIBLE
            val photo = wp.photoAttachments[0]
            val scaleFactor = photo.width!!.toFloat() / 1f / screenWidth.toFloat()
            vholder.ivPreview.layoutParams.height = (photo.height!! / scaleFactor).toInt()
            vholder.ivPreview.layoutParams.width = screenWidth

            Picasso.with(context)
                    .load(urls[0])
                    .placeholder(R.drawable.placeholder)
                    .into(vholder.ivPreview)

            vholder.ivPreview.setOnClickListener { ImageViewerActivity.viewImages(context, wp.photoAttachments) }
        } else {
            vholder.ivPreview.visibility = View.GONE
        }
        if (wp.photoAttachments.size > 1) {
            vholder.tvPageCounter.visibility = View.VISIBLE
            vholder.tvPageCounter.text = "${wp.photoAttachments.size}"
        } else {
            vholder.tvPageCounter.visibility = View.GONE
        }
        val like = ContextCompat.getDrawable(context, R.drawable.ic_favorite_fill)
        val nLike = ContextCompat.getDrawable(context, R.drawable.ic_favorite)
        Style.forDrawable(like, Style.MAIN_TAG)
        Style.forDrawable(nLike, Style.MAIN_TAG)

        if (wp.likes!!.userLikes == 1) {
            vholder.ivLike.setImageDrawable(like)
        } else {
            vholder.ivLike.setImageDrawable(nLike)
        }
        vholder.tvLikes.text = wp.likes.count.toString()

        vholder.ivLike.setOnClickListener {
            vholder.ivLike.setImageDrawable(like)
            onLike?.invoke(position)
        }

        val views = ContextCompat.getDrawable(context, R.drawable.ic_eye)
        Style.forDrawable(views, Style.MAIN_TAG)
        vholder.ivViews.setImageDrawable(views)

        vholder.tvViews.text = shortifyNumber(wp.views?.count ?: 0)

    }

    private fun getUrls(photos: MutableList<Photo>): MutableList<String> {
        val urls = ArrayList<String>()
        for (i in photos.indices) {
            urls.add(photos[i].maxPhoto)
        }
        return urls
    }

    private fun getTextPreview(full: String): String {
        val allowedLength = 400
        var pos = 0
        var count = 0
        val sb = StringBuffer()
        while (count < allowedLength && pos < full.length) {
            val c = full[pos]
            sb.append(c)
            if (c == '\n') {
                count += 30
            } else {
                count++
            }
            pos++
        }
        return if (count < allowedLength) {
            full
        } else {
            sb.append("...").toString()
        }
    }

    internal inner class FeedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        @BindView(R.id.rlHeader)
        lateinit var rlHeader: RelativeLayout
        @BindView(R.id.civAvatar)
        lateinit var civAvatar: CircleImageView
        @BindView(R.id.tvTitle)
        lateinit var tvTitle: TextView
        @BindView(R.id.tvPost)
        lateinit var tvPost: TextView
        @BindView(R.id.ivPreview)
        lateinit var ivPreview: ImageView
        @BindView(R.id.tvDate)
        lateinit var tvDate: TextView
        @BindView(R.id.ivLike)
        lateinit var ivLike: ImageView
        @BindView(R.id.tvLikes)
        lateinit var tvLikes: TextView
        @BindView(R.id.ivViews)
        lateinit var ivViews: ImageView
        @BindView(R.id.tvViews)
        lateinit var tvViews: TextView
        @BindView(R.id.tvPageCounter)
        lateinit var tvPageCounter: TextView

        init {
            ButterKnife.bind(this, itemView)
        }
    }
}
