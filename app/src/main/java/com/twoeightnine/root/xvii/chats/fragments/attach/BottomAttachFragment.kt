package com.twoeightnine.root.xvii.chats.fragments.attach

import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.view.View
import butterknife.BindView
import butterknife.ButterKnife
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.CommonPagerAdapter
import com.twoeightnine.root.xvii.chats.Titleable
import com.twoeightnine.root.xvii.fragments.BaseFragment
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.model.Attachment

class BottomAttachFragment: BaseFragment(), Titleable {

    companion object {
        fun newInstance(listener: ((MutableList<Attachment>) -> Unit)?): BottomAttachFragment {
            val frag = BottomAttachFragment()
            frag.listener = listener
            return frag
        }
    }

    override fun getTitle() = getString(R.string.vk_materials)

    @BindView(R.id.tabsAttachments)
    lateinit var tabs: TabLayout
    @BindView(R.id.viewPager)
    lateinit var viewPager: ViewPager

    lateinit var pagerAdapter: CommonPagerAdapter
    var listener: ((MutableList<Attachment>) -> Unit)? = null

    override fun bindViews(view: View) {
        super.bindViews(view)
        ButterKnife.bind(this, view)
        initAdapter()
    }

    fun initAdapter() {
        pagerAdapter = CommonPagerAdapter(childFragmentManager)
        pagerAdapter.add(PhotoAttachFragment.newInstance(listener), getString(R.string.photos))
        pagerAdapter.add(DocAttachFragment.newInstance({ listener?.invoke(mutableListOf(it)) }), getString(R.string.docs))
        pagerAdapter.add(VideoAttachFragment.newInstance({ listener?.invoke(mutableListOf(it)) }), getString(R.string.videos))
        viewPager.adapter = pagerAdapter
        tabs.setupWithViewPager(viewPager, true)
        Style.forTabLayout(tabs)
    }

    override fun getLayout() = R.layout.fragment_attachments
}