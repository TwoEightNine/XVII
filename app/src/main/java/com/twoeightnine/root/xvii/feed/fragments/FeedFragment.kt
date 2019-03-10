package com.twoeightnine.root.xvii.feed.fragments

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import butterknife.BindView
import butterknife.ButterKnife
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.dagger.ApiService
import com.twoeightnine.root.xvii.feed.FeedAdapter
import com.twoeightnine.root.xvii.fragments.BaseFragment
import com.twoeightnine.root.xvii.fragments.WallPostFragment
import com.twoeightnine.root.xvii.managers.Lg
import com.twoeightnine.root.xvii.managers.Session
import com.twoeightnine.root.xvii.model.Group
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.utils.screenWidth
import com.twoeightnine.root.xvii.utils.showError
import com.twoeightnine.root.xvii.utils.subscribeSmart
import javax.inject.Inject

class FeedFragment: BaseFragment() {

    @BindView(R.id.rvFeed)
    lateinit var rvFeed: androidx.recyclerview.widget.RecyclerView

    @Inject
    lateinit var api: ApiService

    private lateinit var adapter: FeedAdapter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        updateTitle("Scroll feeed everyday")
    }

    override fun bindViews(view: View) {
        super.bindViews(view)
        ButterKnife.bind(this, view)
        App.appComponent?.inject(this)
        initAdapter()
    }

    private fun initAdapter() {
        adapter = FeedAdapter(
                safeActivity,
                { loadMore() },
                ::onClick,
                ::onLike,
                screenWidth(safeActivity)
        )
        rvFeed.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
        rvFeed.adapter = adapter
    }

    private fun onClick(pos: Int) {
        val wp = adapter.items[pos]
        val src = if (wp.sourceId == 0) wp.ownerId else wp.sourceId
        val id = if (wp.postId == 0) wp.id else wp.postId
        rootActivity.loadFragment(WallPostFragment.newInstance("${src}_$id"))
    }

    private fun onLike(pos: Int) {
        val wp = adapter.items[pos]
        api.like(wp.owner(), wp.item())
                .subscribeSmart({
                    response ->
                    adapter.items[pos].likes?.count = response.likes
                    adapter.items[pos].likes?.userLikes = 1
                    adapter.notifyItemChanged(pos)
                }, {
                    adapter.items[pos].likes?.userLikes = 0
                    adapter.notifyItemChanged(pos)
                    showError(activity, it)
                })
    }

    private fun loadMore() {
        getFeedObservable()
                .subscribeSmart({
                    response ->
                    val wallPosts = response.items ?: return@subscribeSmart

                    val groups = response.groups
                    val profiles = response.profiles

                    for (pos in wallPosts.indices) {
                        val wallPost = wallPosts[pos]
                        if (wallPost.sourceId > 0 && profiles != null) {
                            wallPost.profile = getProfileById(profiles, wallPost.sourceId)
                        } else if (wallPost.sourceId < 0 && groups != null) {
                            wallPost.group = getGroupById(groups, -wallPost.sourceId)
                        } else if (wallPost.fromId > 0 && profiles != null) {
                            wallPost.profile = getProfileById(profiles, wallPost.fromId)
                        } else if (wallPost.fromId < 0 && groups != null) {
                            wallPost.group = getGroupById(groups, -wallPost.fromId)
                        }
                    }
                    adapter.stopLoading(wallPosts)
                    adapter.nextFrom = response.nextFrom
                }, {
                    showError(activity, it)
                })
    }

    private fun getGroupById(groups: List<Group>, groupId: Int) =  try {
        groups.filter { it.id == groupId }[0]
    } catch (e: Exception) {
        Lg.wtf("matching groups error for $groupId: ${e.message}")
        null
    }

    private fun getProfileById(profiles: List<User>, userId: Int) = try {
        profiles.filter { it.id == userId }[0]
    } catch (e: Exception) {
        Lg.wtf("matching profiles error for $userId: ${e.message}")
        null
    }

    private fun getFeedObservable() =
            when (state) {
                RECOMM -> api.getRecommended(100)
                FEED -> api.getFeed(50, adapter.nextFrom)
                else -> api.searchFeed(TAGS[state - 2], 200)
            }


    override fun onNew(view: View) {
        loadMore()
    }

    override fun getLayout() = R.layout.fragment_feed

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.clear()
        inflater?.inflate(R.menu.menu_feed, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?) =
        when (item?.itemId) {
            R.id.menu_recommended -> {
                switchState()
                initAdapter()
                loadMore()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    private fun switchState() {
        state = (state + 1) % COUNT
    }

    companion object {
        var state = 0

        const val RECOMM = 0
        const val FEED = 1

        val TAGS = arrayOf("#xvii")
        val COUNT = TAGS.size + 2
    }
}