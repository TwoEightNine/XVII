package com.twoeightnine.root.xvii.fragments

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.squareup.picasso.Picasso
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.dagger.ApiService
import com.twoeightnine.root.xvii.managers.Session
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.model.Attachment
import com.twoeightnine.root.xvii.model.Group
import com.twoeightnine.root.xvii.model.WallPost
import com.twoeightnine.root.xvii.response.WallPostResponse
import com.twoeightnine.root.xvii.utils.*
import com.twoeightnine.root.xvii.views.LoaderView
import de.hdodenhof.circleimageview.CircleImageView
import javax.inject.Inject

class WallPostFragment : BaseFragment() {

    companion object {

        fun newInstance(postId: String): WallPostFragment {
            val frag = WallPostFragment()
            frag.postId = postId
            return frag
        }
    }

    @BindView(R.id.llRoot)
    lateinit var llRoot: LinearLayout
    @BindView(R.id.ivLike)
    lateinit var ivLike: ImageView
    @BindView(R.id.tvLikes)
    lateinit var tvLikes: TextView
    @BindView(R.id.loader)
    lateinit var loader: LoaderView

    var postId: String? = null
    lateinit private var postResponse: WallPostResponse

    @Inject
    lateinit var api: ApiService

    override fun bindViews(view: View) {
        ButterKnife.bind(this, view)
        App.appComponent?.inject(this)
        getWallPostRequest()
    }

    override fun getLayout() = R.layout.fragment_wall_post

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        updateTitle(getString(R.string.wall_post))
    }

    private fun getWallPostRequest() {
        loader.visibility = View.VISIBLE
        api.getWallPostById(postId ?: "")
                .subscribeSmart({
                    response ->
                    loader.visibility = View.GONE
                    postResponse = response
                    if (response.items.size > 0) {
                        fillContent(llRoot)
                        putViews(WallViewHolder(llRoot), response.items[0], 0)
                        initLike(response.items[0])
                    } else {
                        showError(rootActivity, getString(R.string.error))
                    }
                }, {
                    showError(rootActivity, it)
                })
    }

    private fun putViews(holder: WallViewHolder, post: WallPost, level: Int) {
        val group = getGroup(-post.fromId)
        holder.tvTitle.text = group.name
        Picasso.with(rootActivity)
                .loadUrl(group.photo100)
                .into(holder.civAvatar)
        holder.tvDate.text = getTime(post.date, true)
        holder.tvPost.text = post.text
        if (post.attachments != null) {
            for (i in 0 until post.attachments.size) {
                val att = post.attachments[i]
                when (att.type) {

                    Attachment.TYPE_PHOTO -> {
                        val photo = att.photo
                        holder.llContainer.addView(getPhotoWall(photo!!, rootActivity))
                    }

                    Attachment.TYPE_DOC -> {
                        val doc = att.doc
                        if (doc!!.isGif) {
                            holder.llContainer.addView(getGif(doc, rootActivity))
                        } else {
                            holder.llContainer.addView(getDoc(doc, rootActivity))
                        }
                    }

                    Attachment.TYPE_AUDIO -> {
                        val audio = att.audio
                        holder.llContainer.addView(getAudio(audio!!, rootActivity))
                    }

                    Attachment.TYPE_LINK -> {
                        val link = att.link
                        holder.llContainer.addView(getLink(link!!, rootActivity))
                    }

                    Attachment.TYPE_VIDEO -> {
                        val video = att.video
                        holder.llContainer.addView(getVideo(video!!, rootActivity))
                    }
                }
            }
        }

        if (post.copyHistory != null && post.copyHistory.size > 0) {
            fillContent(holder.llContainer)
            putViews(WallViewHolder(holder.llContainer), post.copyHistory[0], level + 1)
        }
    }

    private fun getGroup(fromId: Int): Group {
        for (group in postResponse.groups) {
            if (group.id == fromId) {
                return group
            }
        }
        return Group()
    }

    private fun fillContent(root: ViewGroup) {
        root.addView(View.inflate(rootActivity, R.layout.content_wall_post, null))
    }

    private fun initLike(wp: WallPost) {
        val noLike = ContextCompat.getDrawable(rootActivity, R.drawable.ic_favorite)
        val like = ContextCompat.getDrawable(rootActivity, R.drawable.ic_favorite_fill)
        Style.forDrawable(like, Style.MAIN_TAG)
        Style.forDrawable(noLike, Style.MAIN_TAG)
        if (wp.likes!!.userLikes == 1) {
            ivLike.setImageDrawable(like)
        } else {
            ivLike.setImageDrawable(noLike)
        }
        tvLikes.text = wp.likes.count.toString()

        ivLike.setOnClickListener {
            ivLike.setImageDrawable(like)
            api.like(wp.ownerId, wp.id)
                    .subscribeSmart({
                        response ->
                        tvLikes.text = response.likes.toString()
                    }, {
                        showError(rootActivity, it)
                        ivLike.setImageDrawable(noLike)
                    })
        }
    }


    inner class WallViewHolder(view: View) {
        @BindView(R.id.civAvatar)
        lateinit var civAvatar: CircleImageView
        @BindView(R.id.tvTitle)
        lateinit var tvTitle: TextView
        @BindView(R.id.tvDate)
        lateinit var tvDate: TextView
        @BindView(R.id.tvPost)
        lateinit var tvPost: TextView
        @BindView(R.id.llContainer)
        lateinit var llContainer: LinearLayout

        init {
            ButterKnife.bind(this, view)
        }
    }
}