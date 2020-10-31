package com.twoeightnine.root.xvii.web

import android.os.Bundle
import android.os.Environment
import android.view.View
import com.bumptech.glide.Glide
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.model.attachments.Doc
import com.twoeightnine.root.xvii.utils.*
import kotlinx.android.synthetic.main.activity_gif_viewer.*
import java.io.File
import javax.inject.Inject

class GifViewerFragment : BaseFragment() {

    private val doc by lazy {
        requireArguments().getParcelable<Doc>(ARG_DOC)
    }

    private val permissionHelper by lazy {
        PermissionHelper(this)
    }

    @Inject
    lateinit var apiUtils: ApiUtils

    override fun getLayoutId(): Int = R.layout.activity_gif_viewer

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        App.appComponent?.inject(this)
        val doc = doc ?: return
        val url = doc.url ?: return

        tvTitle.text = doc.title
        ivGif.setOnClickListener {
            rlControls.toggle()
        }

        btnSaveToDocs.setOnClickListener {
            apiUtils.saveDoc(requireContext(), doc.ownerId, doc.id, doc.accessKey ?: "")
        }
        btnDownload.setOnClickListener {
            permissionHelper.doOrRequest(
                    arrayOf(PermissionHelper.WRITE_STORAGE, PermissionHelper.READ_STORAGE),
                    R.string.no_access_to_storage,
                    R.string.need_access_to_storage
            ) {
                val fileName = "${doc.title}_${doc.id}.${doc.ext}"
                val file = File(SAVE_FILE, fileName)
                DownloadUtils.download(requireContext(), file, url)
            }
        }
        ivBack.setOnClickListener { onBackPressed() }

        Glide.with(ivGif)
                .load(url)
                .into(ivGif)

        setStatusBarLight(isLight = false)
        rlTop.setTopInsetPadding(resources.getDimensionPixelSize(R.dimen.toolbar_height))
        rlBottom.setBottomInsetPadding()
    }

    companion object {

        private const val SAVE_DIR = "vk"
        private val SAVE_FILE = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), SAVE_DIR)

        private const val ARG_DOC = "doc"

        fun createArgs(doc: Doc) = Bundle().apply {
            putParcelable(ARG_DOC, doc)
        }
    }
}