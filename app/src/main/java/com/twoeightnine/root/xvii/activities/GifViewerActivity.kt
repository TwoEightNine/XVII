package com.twoeightnine.root.xvii.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import butterknife.BindView
import butterknife.ButterKnife
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.fragments.WebFragment
import com.twoeightnine.root.xvii.utils.*
import com.twoeightnine.root.xvii.views.LoaderView
import java.io.File
import javax.inject.Inject

/**
 * now gif viewer uses WebView due to vk jackass hq
 * i hate them
 * je les hais
 */
class GifViewerActivity: AppCompatActivity() {

    @BindView(R.id.ivGif)
    lateinit var ivGif: ImageView
    @BindView(R.id.tvTitle)
    lateinit var tvTitle: TextView
    @BindView(R.id.loader)
    lateinit var loader: ProgressBar
    @BindView(R.id.btnSaveToDocs)
    lateinit var btnSaveToDocs: Button
    @BindView(R.id.btnDownload)
    lateinit var btnDownload: Button
    @BindView(R.id.rlTop)
    lateinit var rlTop: RelativeLayout
    @BindView(R.id.llBottom)
    lateinit var rlBottom: LinearLayout

    @Inject
    lateinit var apiUtils: ApiUtils

    private lateinit var url: String
    private lateinit var accessKey: String
    private lateinit var title: String
    private lateinit var preview: String

    private var ownerId = 0
    private var docId = 0
    private var removeAfter = true

    private lateinit var name: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gif_viewer)
        App.appComponent?.inject(this)
        ButterKnife.bind(this)
        if (intent.extras != null) {
            url = intent.extras.getString(URL)
            accessKey = intent.extras.getString(ACCESS_KEY)
            title = intent.extras.getString(TITLE)
            preview = intent.extras.getString(PREVIEW)
            ownerId = intent.extras.getInt(OWNER_ID)
            docId = intent.extras.getInt(DOC_ID)
        } else {
            finish()
        }
        tvTitle.text = title
        name = getNameFromTitle(title)
        name = "file://${Environment.getExternalStoragePublicDirectory(DownloadFileAsyncTask.DEFAULT_PATH)}/$name"
        ivGif.setOnClickListener {
            visibilitor(rlTop)
            visibilitor(rlBottom)
        }
        btnSaveToDocs.setOnClickListener {
            apiUtils.saveDoc(this, ownerId, docId, accessKey)
        }
        btnDownload.setOnClickListener {
            downloadFile(this, url, name, "gif", { showCommon(this, getString(R.string.doenloaded, getString(R.string.doc))) })
            removeAfter = false
        }

        supportFragmentManager
                .beginTransaction()
                .add(R.id.flContent, WebFragment.newInstance(url))
                .commit()
    }

    private fun getNameFromTitle(title: String) =
        if (title.endsWith(GIF_EXT, true)) {
            getNameFromUrl(title)
        } else {
            "${getNameFromUrl(title)}$GIF_EXT"
        }

    private fun visibilitor(vg: ViewGroup) {
        vg.visibility = if (vg.visibility == View.VISIBLE) View.INVISIBLE else View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        if (removeAfter) {
            File(name).delete()
        }
    }

    companion object {

        val URL = "url"
        val ACCESS_KEY = "accessKey"
        val TITLE = "title"
        val PREVIEW = "preview"
        val OWNER_ID = "ownerId"
        val DOC_ID = "docId"
        val GIF_EXT = ".gif"

        fun showGif(context: Context, ownerId: Int, docId: Int, url: String, accessKey: String, title: String, preview: String) {
            val intent = Intent(context, GifViewerActivity::class.java)
            intent.putExtra(URL, url)
            intent.putExtra(ACCESS_KEY, accessKey)
            intent.putExtra(TITLE, title)
            intent.putExtra(PREVIEW, preview)
            intent.putExtra(OWNER_ID, ownerId)
            intent.putExtra(DOC_ID, docId)
            context.startActivity(intent)
        }
    }
}