package com.twoeightnine.root.xvii.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.fragments.WebFragment
import com.twoeightnine.root.xvii.model.attachments.Doc
import com.twoeightnine.root.xvii.utils.ApiUtils
import com.twoeightnine.root.xvii.utils.toggle
import kotlinx.android.synthetic.main.activity_gif_viewer.*
import javax.inject.Inject

/**
 * now gif viewer uses WebView due to vk jackass hq
 * i hate them
 * je les hais
 */
class GifViewerActivity : AppCompatActivity() {

    @Inject
    lateinit var apiUtils: ApiUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gif_viewer)
        App.appComponent?.inject(this)

        intent?.extras?.apply {
            val url = getString(URL) ?: return
            val title = getString(TITLE)
            val accessKey = getString(ACCESS_KEY) ?: ""
            val ownerId = getInt(OWNER_ID)
            val docId = getInt(DOC_ID)

            tvTitle.text = title
            ivGif.setOnClickListener {
                rlTop.toggle()
                llBottom.toggle()
            }
            btnSaveToDocs.setOnClickListener {
                apiUtils.saveDoc(this@GifViewerActivity, ownerId, docId, accessKey)
            }

            supportFragmentManager
                    .beginTransaction()
                    .add(R.id.flContent, WebFragment.newInstance(url))
                    .commit()
        }
    }

    companion object {

        const val URL = "url"
        const val ACCESS_KEY = "accessKey"
        const val TITLE = "title"
        const val OWNER_ID = "ownerId"
        const val DOC_ID = "docId"

        fun showGif(context: Context?, doc: Doc) {
            context ?: return

            val intent = Intent(context, GifViewerActivity::class.java).apply {
                putExtra(URL, doc.url)
                putExtra(ACCESS_KEY, doc.accessKey)
                putExtra(TITLE, doc.title)
                putExtra(OWNER_ID, doc.ownerId)
                putExtra(DOC_ID, doc.id)
            }
            context.startActivity(intent)
        }
    }
}