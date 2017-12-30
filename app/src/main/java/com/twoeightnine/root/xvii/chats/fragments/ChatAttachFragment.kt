package com.twoeightnine.root.xvii.chats.fragments

import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import butterknife.BindView
import butterknife.ButterKnife
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.fragments.BaseFragment
import com.twoeightnine.root.xvii.managers.Style

class ChatAttachFragment: BaseFragment() {

    @BindView(R.id.rlBack)
    lateinit var rlBack: RelativeLayout
    @BindView(R.id.ivMemes)
    lateinit var ivMemes: ImageView
    @BindView(R.id.ivStickers)
    lateinit var ivStickers: ImageView
    @BindView(R.id.ivCamera)
    lateinit var ivCamera: ImageView
    @BindView(R.id.ivGallery)
    lateinit var ivGallery: ImageView
    @BindView(R.id.ivVoice)
    lateinit var ivVoice: ImageView
    @BindView(R.id.ivMaterials)
    lateinit var ivMaterials: ImageView

    private var attachListener: ((Int) -> Unit)? = null

    companion object{

        fun newInstance(listener: (Int) -> Unit): ChatAttachFragment {
            val fragment = ChatAttachFragment()
            fragment.attachListener = listener
            return fragment
        }

    }

    override fun bindViews(view: View) {
        ButterKnife.bind(this, view)
        ivMemes.setOnClickListener { attachListener?.invoke(R.id.ivMemes) }
        ivStickers.setOnClickListener { attachListener?.invoke(R.id.ivStickers) }
        ivCamera.setOnClickListener { attachListener?.invoke(R.id.ivCamera) }
        ivGallery.setOnClickListener { attachListener?.invoke(R.id.ivGallery) }
        ivVoice.setOnClickListener { attachListener?.invoke(R.id.ivVoice) }
        ivMaterials.setOnClickListener { attachListener?.invoke(R.id.ivMaterials) }
        Style.forAll(rlBack)
    }

    override fun getLayout() = R.layout.fragment_chat_attach
}