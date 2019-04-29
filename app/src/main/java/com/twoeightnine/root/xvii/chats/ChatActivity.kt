package com.twoeightnine.root.xvii.chats

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.activities.ContentActivity
import com.twoeightnine.root.xvii.dialogs.models.Dialog
import com.twoeightnine.root.xvii.model.User

class ChatActivity : ContentActivity() {

    override fun getLayoutId() = R.layout.activity_content

    override fun getFragment(args: Bundle?): Fragment {
        val forwarded = args?.getString(FORWARDED) ?: ""
        val dialog = args?.getParcelable(DIALOG) ?: Dialog()
        return ChatFragment.newInstance(dialog, forwarded)
    }

    companion object {
        const val DIALOG = "dialog"
        const val FORWARDED = "forwarded"

        fun launch(context: Context?, userId: Int, title: String,
                   avatar: String? = null, forwarded: String = "") {
            launch(context, Dialog(
                    peerId = userId,
                    title = title,
                    photo = avatar
            ), forwarded)

        }

        fun launch(context: Context?, dialog: Dialog, forwarded: String = "") {
            context ?: return

            context.startActivity(Intent(context, ChatActivity::class.java).apply {
                putExtra(FORWARDED, forwarded)
                putExtra(DIALOG, dialog)
                flags = flags or Intent.FLAG_ACTIVITY_CLEAR_TOP
            })
        }

        fun launch(context: Context?, user: User) {
            launch(context, Dialog(
                    peerId = user.id,
                    title = user.fullName,
                    photo = user.photo100
            ))
        }
    }
}