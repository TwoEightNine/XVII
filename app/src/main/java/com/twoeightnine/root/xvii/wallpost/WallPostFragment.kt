package com.twoeightnine.root.xvii.wallpost

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.model.Group
import com.twoeightnine.root.xvii.model.WallPost
import com.twoeightnine.root.xvii.model.attachments.Attachment
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.network.response.WallPostResponse
import com.twoeightnine.root.xvii.photoviewer.ImageViewerActivity
import com.twoeightnine.root.xvii.utils.*
import de.hdodenhof.circleimageview.CircleImageView
import global.msnthrp.xvii.uikit.extensions.applyBottomInsetPadding
import global.msnthrp.xvii.uikit.extensions.show
import kotlinx.android.synthetic.main.content_wall_post.view.*
import kotlinx.android.synthetic.main.fragment_wall_post.*
import javax.inject.Inject

class WallPostFragment : BaseFragment() {

    private val postId by lazy { arguments?.getString(ARG_POST_ID) }
    private lateinit var postResponse: WallPostResponse

    @Inject
    lateinit var api: ApiService

    @Inject
    lateinit var apiUtils: ApiUtils

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        App.appComponent?.inject(this)
        getWallPostRequest()
    }

    override fun getLayoutId() = R.layout.fragment_wall_post

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        svContent.applyBottomInsetPadding()
    }

    override fun getMenu(): Int = R.menu.menu_wall_post

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.menu_open_url -> {
            simpleUrlIntent(context, "$WALL_POST_URL$postId")
            true
        }
        else -> super.onOptionsItemSelected(item)
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
                        showError(context, getString(R.string.error))
                    }
                }, {
                    showError(context, it)
                })
    }

    private fun putViews(holder: WallViewHolder, post: WallPost, level: Int) {
        val group = getGroup(-post.fromId)
        holder.tvTitle.text = group.name
        if (Prefs.lowerTexts) {
            holder.tvTitle.lower()
        }
        holder.civAvatar.load(group.photo100)
        holder.tvDate.text = getTime(post.date, withSeconds = Prefs.showSeconds)
        holder.tvPost.text = post.text
        post.attachments?.forEach { attachment ->
            when (attachment.type) {

                Attachment.TYPE_PHOTO -> attachment.photo?.also {
                    val act = activity ?: return@also
                    holder.llContainer.addView(getPhotoWall(it, act) { photo ->
                        val photos = ArrayList(post.getPhoto())
                        val position = photos.indexOf(photo)
                        ImageViewerActivity.viewImages(context, photos, position)
                    })
                }

                Attachment.TYPE_DOC -> attachment.doc?.also { doc ->
                    context?.also {
                        if (doc.isGif) {
                            holder.llContainer.addView(getGif(doc, it))
                        } else {
                            holder.llContainer.addView(getDoc(doc, it))
                        }
                    }
                }

                Attachment.TYPE_AUDIO -> attachment.audio?.also { audio ->
                    context?.also {
                        holder.llContainer.addView(getAudio(audio, it))
                    }
                }


                Attachment.TYPE_LINK -> attachment.link?.also { link ->
                    context?.also {
                        holder.llContainer.addView(getLink(link, it))
                    }
                }

                Attachment.TYPE_POLL -> attachment.poll?.also { poll ->
                    context?.also {
                        holder.llContainer.addView(getPoll(poll, it))
                    }
                }

                Attachment.TYPE_VIDEO -> attachment.video?.also { video ->
                    activity?.also {
                        holder.llContainer.addView(getVideo(video, it) { video ->
                            apiUtils.openVideo(it, video)
                        })
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
        root.addView(View.inflate(context, R.layout.content_wall_post, null))
    }

    private fun initLike(wp: WallPost) {
        val likes = wp.likes ?: return
        val context = context ?: return

        val noLike = ContextCompat.getDrawable(context, R.drawable.ic_no_like)
        val like = ContextCompat.getDrawable(context, R.drawable.ic_like)
        if (likes.isUserLiked) {
            ivLike.setImageDrawable(like)
        } else {
            ivLike.setImageDrawable(noLike)
        }
        tvLikes.text = likes.count.toString()
        val flowableLike = api.like(wp.ownerId, wp.id)
        val flowableUnlike = api.unlike(wp.ownerId, wp.id)
        ivLike.setOnClickListener {
            if (!likes.isUserLiked) {
                ivLike.setImageDrawable(like)
                flowableLike
                        .subscribeSmart({ response ->
                            likes.isUserLiked = true
                            tvLikes.text = response.likes.toString()
                        }, {
                            showError(context, it)
                            ivLike.setImageDrawable(noLike)
                        })
            } else {
                ivLike.setImageDrawable(noLike)
                flowableUnlike
                        .subscribeSmart({ response ->
                            likes.isUserLiked = false
                            tvLikes.text = response.likes.toString()
                        }, {
                            showError(context, it)
                            ivLike.setImageDrawable(like)
                        })
            }
        }
    }

    companion object {

        const val WALL_POST_URL = "https://vk.com/wall"

        const val ARG_POST_ID = "postId"

        fun newInstance(postId: String): WallPostFragment {
            return WallPostFragment().apply {
                arguments = createArgs(postId)
            }
        }

        fun createArgs(postId: String) = Bundle().apply {
            putString(ARG_POST_ID, postId)
        }
    }

    inner class WallViewHolder(view: View) {

        val civAvatar: CircleImageView = view.civAvatar
        val tvTitle: TextView = view.tvTitle
        val tvDate: TextView = view.tvDate
        val tvPost: TextView = view.tvPost
        val llContainer: LinearLayout = view.llContainer
    }
}