package com.twoeightnine.root.xvii.chats.fragments.attachments

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import butterknife.BindView
import butterknife.ButterKnife
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.CommonPagerAdapter
import com.twoeightnine.root.xvii.fragments.BaseFragment
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.views.LoaderView

class AttachmentsFragment: BaseFragment() {

    @BindView(R.id.tabs)
    lateinit var tabs: TabLayout
    @BindView(R.id.viewPager)
    lateinit var viewPager: ViewPager
    @BindView(R.id.loader)
    lateinit var loader: LoaderView

    lateinit var adapter: CommonPagerAdapter

    companion object {
        fun newInstance(peerId: Int): AttachmentsFragment {
            val frag = AttachmentsFragment()
            frag.peerId = peerId
            return frag
        }
    }

    var peerId = 0

    override fun bindViews(view: View) {
        super.bindViews(view)
        ButterKnife.bind(this, view)
        initAdapter()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        updateTitle(getString(R.string.attachments))
    }

    fun initAdapter() {
        adapter = CommonPagerAdapter(childFragmentManager)
        adapter.add(PhotoAttachmentsFragment.newInstance(peerId), getString(R.string.photos))
        adapter.add(VideoAttachmentsFragment.newInstance(peerId), getString(R.string.videos))
        adapter.add(LinkAttachmentsFragment.newInstance(peerId), getString(R.string.links))
        adapter.add(DocAttachmentsFragment.newInstance(peerId), getString(R.string.docs))
        viewPager.adapter = adapter
        tabs.setupWithViewPager(viewPager, true)
        viewPager.offscreenPageLimit = 3
        Style.forTabLayout(tabs)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.clear()
    }

    override fun getLayout() = R.layout.fragment_attachments_history
}