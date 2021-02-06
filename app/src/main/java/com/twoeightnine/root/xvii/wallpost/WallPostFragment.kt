package com.twoeightnine.root.xvii.wallpost

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.chats.attachments.AttachmentsInflater
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.model.Group
import com.twoeightnine.root.xvii.model.WallPost
import com.twoeightnine.root.xvii.model.attachments.Doc
import com.twoeightnine.root.xvii.model.attachments.Video
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.network.response.WallPostResponse
import com.twoeightnine.root.xvii.uikit.XviiAvatar
import com.twoeightnine.root.xvii.utils.*
import global.msnthrp.xvii.uikit.extensions.applyBottomInsetPadding
import global.msnthrp.xvii.uikit.extensions.hide
import global.msnthrp.xvii.uikit.extensions.lowerIf
import global.msnthrp.xvii.uikit.extensions.show
import kotlinx.android.synthetic.main.content_wall_post.view.*
import kotlinx.android.synthetic.main.content_wall_post.view.civAvatar
import kotlinx.android.synthetic.main.fragment_wall_post.*
import kotlinx.android.synthetic.main.toolbar2.view.*
import javax.inject.Inject

class WallPostFragment : BaseFragment() {

    private val postId by lazy { arguments?.getString(ARG_POST_ID) }
    private lateinit var postResponse: WallPostResponse

    private val attachmentsInflater by lazy {
        AttachmentsInflater(requireContext(), WallPostCallback(requireContext()))
    }

    @Inject
    lateinit var api: ApiService

    @Inject
    lateinit var apiUtils: ApiUtils

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        App.appComponent?.inject(this)
        getWallPostRequest()
        svContent.applyBottomInsetPadding()
    }

    override fun getLayoutId() = R.layout.fragment_wall_post

    override fun getMenu(): Int = R.menu.menu_wall_post

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.menu_open_url -> {
            BrowsingUtils.openUrl(context, "$WALL_POST_URL$postId")
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun getWallPostRequest() {
        loader.show()
        api.getWallPostById(postId ?: "")
                .subscribeSmart({ response ->
                    loader.hide()
                    postResponse = response
                    if (response.items.size > 0) {
                        fillContent(llRoot)
                        putViews(WallViewHolder(llRoot), response.items[0])
                    } else {
                        showError(context, getString(R.string.error))
                    }
                }, {
                    showError(context, it)
                })
    }

    private fun putViews(holder: WallViewHolder, post: WallPost, level: Int = 0) {
        val group = getGroup(-post.fromId)
        if (level == 0) {
            xviiToolbar.tvChatTitle.text = group.name
            xviiToolbar.tvChatTitle.lowerIf(Prefs.lowerTexts)

            xviiToolbar.civAvatar.load(group.photo100)
            xviiToolbar.tvSubtitle.text = getTime(post.date, withSeconds = Prefs.showSeconds)
            holder.rlHeader.hide()
        } else {
            holder.tvTitle.text = group.name
            holder.tvTitle.lowerIf(Prefs.lowerTexts)

            holder.civAvatar.load(group.photo100)
            holder.tvDate.text = getTime(post.date, withSeconds = Prefs.showSeconds)
        }
        holder.tvPost.text = post.text
        attachmentsInflater.createViewsFor(post)
                .forEach(holder.llContainer::addView)

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

    private inner class WallViewHolder(view: View) {

        val rlHeader: RelativeLayout = view.rlHeader
        val civAvatar: XviiAvatar = view.civAvatar
        val tvTitle: TextView = view.tvTitle
        val tvDate: TextView = view.tvDate
        val tvPost: TextView = view.tvPost
        val llContainer: LinearLayout = view.llContainer
    }

    private inner class WallPostCallback(context: Context) : AttachmentsInflater.DefaultCallback(context) {

        override fun onEncryptedDocClicked(doc: Doc) {
        }

        override fun onVideoClicked(video: Video) {
            context?.also {
                apiUtils.openVideo(it, video)
            }
        }
    }
}