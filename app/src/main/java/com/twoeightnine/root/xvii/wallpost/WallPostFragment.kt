/*
 * xvii - messenger for vk
 * Copyright (C) 2021  TwoEightNine
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
import com.twoeightnine.root.xvii.base.FragmentPlacementActivity.Companion.startFragment
import com.twoeightnine.root.xvii.chats.attachments.AttachmentsInflater
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.model.Group
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.model.WallPost
import com.twoeightnine.root.xvii.model.attachments.Doc
import com.twoeightnine.root.xvii.model.attachments.Video
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.network.response.WallPostResponse
import com.twoeightnine.root.xvii.report.ReportFragment
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
        R.id.menu_report -> {
            if (::postResponse.isInitialized) {
                postResponse.items.firstOrNull()?.also { wallPost ->
                    val args = ReportFragment.createArgs(wallPost = wallPost)
                    startFragment<ReportFragment>(args)
                }
            }
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

        val (title, avatar) = when{
            post.fromId < 0 -> {
                val group = getGroup(-post.fromId)
                Pair(group.name, group.photo100)
            }
            else -> {
                val user = getUser(post.fromId)
                Pair(user.getTitle(), user.photo100)
            }
        }

        if (level == 0) {
            xviiToolbar.tvChatTitle.text = title
            xviiToolbar.tvChatTitle.lowerIf(Prefs.lowerTexts)

            xviiToolbar.civAvatar.load(avatar)
            xviiToolbar.tvSubtitle.text = getTime(post.date, withSeconds = Prefs.showSeconds)
            holder.rlHeader.hide()
        } else {
            holder.tvTitle.text = title
            holder.tvTitle.lowerIf(Prefs.lowerTexts)

            holder.civAvatar.load(avatar)
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

    private fun getUser(fromId: Int): User {
        for (user in postResponse.profiles) {
            if (user.id == fromId) {
                return user
            }
        }
        return User()
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

    private inner class WallPostCallback(context: Context)
        : AttachmentsInflater.DefaultCallback(context, PermissionHelper(this)) {

        override fun onEncryptedDocClicked(doc: Doc) {
        }

        override fun onVideoClicked(video: Video) {
            context?.also {
                apiUtils.openVideo(it, video)
            }
        }
    }
}