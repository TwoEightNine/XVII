package com.twoeightnine.root.xvii.chats.fragments

import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.fragments.BaseFragment
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.views.emoji.EmojiEditText

class ChatSendFragment: BaseFragment() {

    companion object{

        fun newInstance(listener: (String) -> Unit,
                        longListener: (String) -> Boolean,
                        typingListener: () -> Unit,
                        emojiListener: () -> Unit,
                        attachmentsListener: () -> Unit): ChatSendFragment {
            val fragment = ChatSendFragment()
            fragment.sendListener = listener
            fragment.longSendListener = longListener
            fragment.typingListener = typingListener
            fragment.emojiListener = emojiListener
            fragment.attachmentsListener = attachmentsListener
            return fragment
        }
    }

    private var sendListener: ((String) -> Unit)? = null
    private var longSendListener: ((String) -> Boolean)? = null
    private var typingListener: (() -> Unit)? = null
    private var emojiListener: (() -> Unit)? = null
    private var attachmentsListener: (() -> Unit)? = null

    @BindView(R.id.rlBack)
    lateinit var rlBack: RelativeLayout
    @BindView(R.id.etInput)
    lateinit var etInput: EmojiEditText
    @BindView(R.id.ivSend)
    lateinit var ivSend: ImageView
    @BindView(R.id.tvAttachCount)
    lateinit var tvAttachCount: TextView
    @BindView(R.id.rlInputContainer)
    lateinit var rlInputContainer: RelativeLayout
    @BindView(R.id.rlAttachContainer)
    lateinit var rlAttachContainer: RelativeLayout
    @BindView(R.id.ivEmoji)
    lateinit var ivEmoji: ImageView

    override fun bindViews(view: View) {
        ButterKnife.bind(this, view)
        ivSend.setOnClickListener { sendListener?.invoke(etInput.text.toString()) }
        ivSend.setOnLongClickListener { longSendListener?.invoke(etInput.text.toString()) ?: false }
        etInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, start: Int, before: Int, count: Int) {
                if (count % 15 == 3) {
                    typingListener?.invoke()
                }

            }
        })
        ivEmoji.setOnClickListener {
            emojiListener?.invoke()
        }
        rlAttachContainer.setOnClickListener { attachmentsListener?.invoke() }
        setCount(0)
        Style.forAll(rlBack)
        if (Build.VERSION.SDK_INT >= 21) {
            val d = rlAttachContainer.background
            Style.forFrame(d)
            rlAttachContainer.background = d
            val d2 = rlInputContainer.background
            Style.forFrame(d2)
            rlInputContainer.background = d2
        }
    }

    override fun getLayout() = R.layout.fragment_chat_send

    fun setText(text: String) {
        etInput.setText(text)
    }

    fun setCount(count: Int) {
        if (count == 0) {
            rlAttachContainer.visibility = View.GONE
        } else {
            rlAttachContainer.visibility = View.VISIBLE
            tvAttachCount.text = "$count"
        }
    }
}