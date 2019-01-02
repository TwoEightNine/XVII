package com.twoeightnine.root.xvii.chats.fragments

import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.FrameLayout
import butterknife.BindView
import butterknife.ButterKnife
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.Titleable
import com.twoeightnine.root.xvii.chats.adapters.StickerCatAdapter
import com.twoeightnine.root.xvii.fragments.BaseFragment
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.model.Attachment
import com.twoeightnine.root.xvii.model.StickerPack

class StickersFragment: BaseFragment(), Titleable {

    companion object {
        fun newInstance(listener: ((Attachment.Sticker) -> Unit)?): StickersFragment {
            val frag = StickersFragment()
            frag.listener = listener
            return frag
        }
    }

    @BindView(R.id.rvStickers)
    lateinit var rvStickers: RecyclerView
    @BindView(R.id.flStickerContainer)
    lateinit var flContainer: FrameLayout

    var listener: ((Attachment.Sticker) -> Unit)? = null

    private lateinit var adapter: StickerCatAdapter

    override fun getTitle() = getString(R.string.stickers)

    override fun bindViews(view: View) {
        super.bindViews(view)
        ButterKnife.bind(this, view)
        initAdapter()
        if (Prefs.recentStickers.isNotEmpty()) {
            loadFragment(StickerGridFragment.newInstance(adapter.items[0], listener))
        } else if (Prefs.availableStickers.isNotEmpty()) {
            loadFragment(StickerGridFragment.newInstance(adapter.items[1], listener))
        } else {
            loadFragment(StickerGridFragment.newInstance(adapter.items[2], listener))
        }
    }

    fun initAdapter() {
        adapter = StickerCatAdapter(safeActivity, {
            pack -> loadFragment(StickerGridFragment.newInstance(pack, listener))
        })
        rvStickers.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        fillPack()
        rvStickers.adapter = adapter
    }

    fun loadFragment(frag: Fragment) {
        childFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.enter_left, R.anim.exit_right, R.anim.enter_left, R.anim.exit_right)
                .replace(R.id.flStickerContainer, frag)
                .commit()
    }

    override fun getLayout() = R.layout.fragment_stickers

    private fun fillPack() { //im sorry
        adapter.add(StickerPack.RECENT)
        adapter.add(StickerPack.AVAILABLE)
        adapter.add(StickerPack(1, 48))
        adapter.add(StickerPack(49, 48))
        adapter.add(StickerPack(97, 32))
        adapter.add(StickerPack(129, 40))
        adapter.add(StickerPack(169, 40))
        adapter.add(StickerPack(209, 24))
        adapter.add(StickerPack(233, 32))
        adapter.add(StickerPack(265, 32))
        adapter.add(StickerPack(297, 40))
        adapter.add(StickerPack(337, 9))
        adapter.add(StickerPack(346, 32))
        adapter.add(StickerPack(378, 48))
        adapter.add(StickerPack(426, 40))
        adapter.add(StickerPack(466, 48))
        adapter.add(StickerPack(514, 24))
        adapter.add(StickerPack(538, 8))
        adapter.add(StickerPack(546, 40))
        adapter.add(StickerPack(586, 34))
        adapter.add(StickerPack(620, 48))
        adapter.add(StickerPack(668, 37))
        adapter.add(StickerPack(705, 41))
        adapter.add(StickerPack(746, 16))
        adapter.add(StickerPack(762, 32))
        adapter.add(StickerPack(794, 32))
        adapter.add(StickerPack(826, 32))
        adapter.add(StickerPack(858, 32))
        adapter.add(StickerPack(890, 24))
        adapter.add(StickerPack(914, 24))
        adapter.add(StickerPack(938, 32))
        adapter.add(StickerPack(970, 49))
        adapter.add(StickerPack(1019, 32))
        adapter.add(StickerPack(1051, 24))
        adapter.add(StickerPack(1075, 40))
        adapter.add(StickerPack(1115, 48))
        adapter.add(StickerPack(1163, 25))
        adapter.add(StickerPack(1188, 16))
        adapter.add(StickerPack(1204, 24))
        adapter.add(StickerPack(1228, 26))
        adapter.add(StickerPack(1254, 24))
        adapter.add(StickerPack(1278, 25))
        adapter.add(StickerPack(1303, 23))
        adapter.add(StickerPack(1326, 40))
        adapter.add(StickerPack(1366, 32))
        adapter.add(StickerPack(1398, 42))
        adapter.add(StickerPack(1440, 44))
        adapter.add(StickerPack(1484, 32))
        adapter.add(StickerPack(1516, 24))
        adapter.add(StickerPack(1541, 32))
        adapter.add(StickerPack(1573, 32))
        adapter.add(StickerPack(1605, 16))
        adapter.add(StickerPack(1621, 24))
        adapter.add(StickerPack(1677, 48))
        adapter.add(StickerPack(1725, 32))
        adapter.add(StickerPack(1757, 32))
        adapter.add(StickerPack(1789, 24))
        adapter.add(StickerPack(1813, 32))
        adapter.add(StickerPack(1845, 32))
        adapter.add(StickerPack(1877, 32))
        adapter.add(StickerPack(1909, 32))
        adapter.add(StickerPack(1941, 48))
        adapter.add(StickerPack(1990, 32))
        adapter.add(StickerPack(2022, 24))
        adapter.add(StickerPack(2046, 24))
        adapter.add(StickerPack(2070, 32))
        adapter.add(StickerPack(2102, 24))
        adapter.add(StickerPack(2126, 32))
        adapter.add(StickerPack(2158, 24))
        adapter.add(StickerPack(2185, 24))
        adapter.add(StickerPack(2210, 24))
        adapter.add(StickerPack(2234, 41))
        adapter.add(StickerPack(2306, 40))
        adapter.add(StickerPack(2346, 24))
        adapter.add(StickerPack(2370, 48))
        adapter.add(StickerPack(2418, 48))
        adapter.add(StickerPack(2466, 16))
        adapter.add(StickerPack(2487, 24))
        adapter.add(StickerPack(2511, 16))
        adapter.add(StickerPack(2527, 24))
        adapter.add(StickerPack(2551, 24))
        adapter.add(StickerPack(2575, 24))
        adapter.add(StickerPack(2599, 24))
        adapter.add(StickerPack(2623, 24))
        adapter.add(StickerPack(2647, 13))
        adapter.add(StickerPack(2663, 40))
        adapter.add(StickerPack(2703, 40))
        adapter.add(StickerPack(2743, 24))
        adapter.add(StickerPack(2767, 24))
        adapter.add(StickerPack(2791, 24))
        adapter.add(StickerPack(2815, 24))
        adapter.add(StickerPack(2839, 24))
        adapter.add(StickerPack(2863, 32))
        adapter.add(StickerPack(2895, 24))
        adapter.add(StickerPack(2919, 9))
        adapter.add(StickerPack(2928, 24))
        adapter.add(StickerPack(2952, 24))
        adapter.add(StickerPack(2976, 24))
        adapter.add(StickerPack(3000, 8))
        adapter.add(StickerPack(3008, 32))
        adapter.add(StickerPack(3040, 24))
        adapter.add(StickerPack(3064, 7))
        adapter.add(StickerPack(3071, 16))
        adapter.add(StickerPack(3087, 48))
        adapter.add(StickerPack(3135, 40))
        adapter.add(StickerPack(3245, 24))
        adapter.add(StickerPack(3269, 32))
        adapter.add(StickerPack(3301, 40))
        adapter.add(StickerPack(3341, 16))
        adapter.add(StickerPack(3357, 32))
        adapter.add(StickerPack(3389, 48))
        adapter.add(StickerPack(3437, 24))
        adapter.add(StickerPack(3461, 24))
        adapter.add(StickerPack(3485, 40))
        adapter.add(StickerPack(3525, 16))
        adapter.add(StickerPack(3542, 32))
        adapter.add(StickerPack(3574, 16))
        adapter.add(StickerPack(3590, 8))
        adapter.add(StickerPack(3598, 8))
        adapter.add(StickerPack(3606, 32))
        adapter.add(StickerPack(3638, 16))
        adapter.add(StickerPack(3654, 16))
        adapter.add(StickerPack(3670, 8))
        adapter.add(StickerPack(3678, 8))
        adapter.add(StickerPack(3686, 8))
        adapter.add(StickerPack(3694, 24))
        adapter.add(StickerPack(3718, 24))
        adapter.add(StickerPack(3773, 32))
        adapter.add(StickerPack(3805, 32))
        adapter.add(StickerPack(3839, 32))
        adapter.add(StickerPack(3871, 48))
        adapter.add(StickerPack(3923, 24))
        adapter.add(StickerPack(3951, 40))
        adapter.add(StickerPack(3999, 24))
        adapter.add(StickerPack(4029, 32))
        adapter.add(StickerPack(4067, 16))
        adapter.add(StickerPack(4085, 36))
        adapter.add(StickerPack(4132, 40))
        adapter.add(StickerPack(4172, 24))
        adapter.add(StickerPack(4196, 40))
        adapter.add(StickerPack(4236, 24))
    }
}