package com.twoeightnine.root.xvii.chats.fragments

import android.view.View
import android.widget.GridView
import butterknife.BindView
import butterknife.ButterKnife
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.Titleable
import com.twoeightnine.root.xvii.chats.adapters.MemeAdapter
import com.twoeightnine.root.xvii.fragments.BaseFragment
import com.twoeightnine.root.xvii.model.Meme
import com.twoeightnine.root.xvii.utils.time
import io.realm.Realm

class MemeFragment: BaseFragment(), Titleable {

    companion object {
        fun newInstance(listener: ((String) -> Unit)?,
                        selected: MutableList<String>): MemeFragment {
            val frag = MemeFragment()
            frag.listener = listener
            frag.selected = selected
            return frag
        }

        fun newInstance(listener: ((String) -> Unit)?) = newInstance(listener, mutableListOf())
    }

    override fun getTitle() = getString(R.string.meme_storage)

    @BindView(R.id.gvMemes)
    lateinit var gvMemes: GridView

    lateinit private var adapter: MemeAdapter

    var listener: ((String) -> Unit)? = null
    var selected: MutableList<String> = mutableListOf()

    override fun bindViews(view: View) {
        super.bindViews(view)
        ButterKnife.bind(this, view)
        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        if (selected.size == 1) {
            realm.copyToRealm(Meme(selected[0]))
        } else {
            for (pos in selected.indices) {
                val delta = selected.size - pos
                realm.copyToRealm(Meme(time() - delta, selected[pos]) )
            }
        }
        realm.commitTransaction()
        initAdapter()
    }

    fun initAdapter() {
        adapter = MemeAdapter()
        adapter.add(Meme.addMeme)
        val realm = Realm.getDefaultInstance()
        val memes = realm
                .where(Meme::class.java)
                .findAll()
                .toMutableList()
        adapter.add(memes)
        gvMemes.adapter = adapter
        gvMemes.setOnItemClickListener {
            _, _, pos, _ ->
            val meme = adapter.items[pos]
            if (meme.isAddMeme()) {
                listener?.invoke(meme.path ?: "")
            } else {
                listener?.invoke(meme.path ?: "")
            }
        }
    }

    override fun getLayout() = R.layout.fragment_memes
}