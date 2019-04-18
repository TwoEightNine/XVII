package com.twoeightnine.root.xvii.wallpost

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.fragments.BaseOldFragment
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.model.Group
import com.twoeightnine.root.xvii.model.WallPost
import com.twoeightnine.root.xvii.model.attachments.Attachment
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.network.response.WallPostResponse
import com.twoeightnine.root.xvii.photoviewer.ImageViewerActivity
import com.twoeightnine.root.xvii.utils.*
import kotlinx.android.synthetic.main.content_wall_post.view.*
import kotlinx.android.synthetic.main.fragment_wall_post.*
import javax.inject.Inject

class WallPostFragment : BaseOldFragment() {

    private val postId by lazy { arguments?.getString(ARG_POST_ID) }
    private lateinit var postResponse: WallPostResponse

    @Inject
    lateinit var api: ApiService

    @Inject
    lateinit var apiUtils: ApiUtils

    override fun bindViews(view: View) {
        App.appComponent?.inject(this)
        getWallPostRequest()
    }

    override fun getLayout() = R.layout.fragment_wall_post

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        updateTitle(getString(R.string.wall_post))
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.clear()
    }

    private fun getWallPostRequest() {
        loader.show()
        api.getWallPostById(postId ?: "")
                .subscribeSmart({ response ->
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
        holder.civAvatar.load(group.photo100)
        holder.tvDate.text = getTime(post.date, full = true)
        holder.tvPost.text = post.text
        post.attachments?.forEach { attachment ->
            when (attachment.type) {

                Attachment.TYPE_PHOTO -> attachment.photo?.also {
                    holder.llContainer.addView(getPhotoWall(it, rootActivity) { photo ->
                        val photos = ArrayList(post.getPhoto())
                        val position = photos.indexOf(photo)
                        ImageViewerActivity.viewImages(context, photos, position)
                    })
                }

                Attachment.TYPE_DOC -> attachment.doc?.also { doc ->
                    if (doc.isGif) {
                        holder.llContainer.addView(getGif(doc, rootActivity))
                    } else {
                        holder.llContainer.addView(getDoc(doc, rootActivity))
                    }
                }

                Attachment.TYPE_AUDIO -> attachment.audio?.also {
                    holder.llContainer.addView(getAudio(it, rootActivity))
                }


                Attachment.TYPE_LINK -> attachment.link?.also {
                    holder.llContainer.addView(getLink(it, rootActivity))
                }

                Attachment.TYPE_VIDEO -> attachment.video?.also {
                    holder.llContainer.addView(getVideo(it, rootActivity) { video ->
                        apiUtils.openVideo(safeActivity, video)
                    })
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
        val likes = wp.likes ?: return

        val noLike = ContextCompat.getDrawable(rootActivity, R.drawable.ic_favorite)
        val like = ContextCompat.getDrawable(rootActivity, R.drawable.ic_favorite_fill)
        Style.forDrawable(like, Style.MAIN_TAG)
        Style.forDrawable(noLike, Style.MAIN_TAG)
        if (likes.isUserLiked) {
            ivLike.setImageDrawable(like)
        } else {
            ivLike.setImageDrawable(noLike)
        }
        tvLikes.text = likes.count.toString()
        val flowableLike = api.like(wp.ownerId, wp.id)
        val flowableUnlike = api.unlike(wp.ownerId, wp.id)
        ivLike.setOnClickListener {
            if (likes.isUserLiked) {
                ivLike.setImageDrawable(like)
                flowableLike
                        .subscribeSmart({ response ->
                            likes.isUserLiked = true
                            tvLikes.text = response.likes.toString()
                        }, {
                            showError(rootActivity, it)
                            ivLike.setImageDrawable(noLike)
                        })
            } else {
                ivLike.setImageDrawable(noLike)
                flowableUnlike
                        .subscribeSmart({ response ->
                            likes.isUserLiked = false
                            tvLikes.text = response.likes.toString()
                        }, {
                            showError(rootActivity, it)
                            ivLike.setImageDrawable(like)
                        })
            }
        }
    }

    companion object {

        const val ARG_POST_ID = "postId"

        fun newInstance(postId: String): WallPostFragment {
            val frag = WallPostFragment()
            frag.arguments = Bundle().apply {
                putString(ARG_POST_ID, postId)
            }
            return frag
        }
    }

    inner class WallViewHolder(view: View) {

        val civAvatar = view.civAvatar
        val tvTitle = view.tvTitle
        val tvDate = view.tvDate
        val tvPost = view.tvPost
        val llContainer = view.llContainer
    }
}