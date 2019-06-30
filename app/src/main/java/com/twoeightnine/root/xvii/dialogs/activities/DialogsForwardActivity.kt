package com.twoeightnine.root.xvii.dialogs.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment
import com.twoeightnine.root.xvii.activities.ContentActivity
import com.twoeightnine.root.xvii.dialogs.fragments.DialogsForwardFragment
import com.twoeightnine.root.xvii.utils.writeToFileFromContentUri
import java.io.File

class DialogsForwardActivity : ContentActivity() {

    override fun createFragment(intent: Intent?): Fragment {
        var forwarded: String? = null
        var shareText: String? = null
        var shareImage: String? = null
        when {
            intent?.action == Intent.ACTION_SEND && intent.type == "text/plain" -> {
                shareText = intent.getStringExtra(Intent.EXTRA_TEXT)
            }
            intent?.action == Intent.ACTION_SEND
                    && intent.type?.startsWith("image/") == true -> {
                intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.also { uri ->
                    val imageFile = File(cacheDir, "share.jpg")
                    val written = writeToFileFromContentUri(this, imageFile, uri)
                    shareImage = if (written) {
                        imageFile.absolutePath
                    } else {
                        uri.path
                    }
                }
            }
            else -> {
                forwarded = intent?.extras?.getString(DialogsForwardFragment.ARG_FORWARDED)
            }
        }
        return DialogsForwardFragment.newInstance(forwarded, shareText, shareImage)
    }

    companion object {
        fun launch(context: Context?, forwarded: String? = null) {
            if (context == null) return

            context.startActivity(Intent(context, DialogsForwardActivity::class.java).apply {
                if (!forwarded.isNullOrEmpty()) {
                    putExtra(DialogsForwardFragment.ARG_FORWARDED, forwarded)
                }
            })
        }
    }
}