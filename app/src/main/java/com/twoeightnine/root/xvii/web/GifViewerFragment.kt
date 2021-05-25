/*
 * xvii - messenger for vk
 * Copyright (C) 2021  TwoEightNine
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.twoeightnine.root.xvii.web

import android.os.Bundle
import android.os.Environment
import android.view.View
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.model.attachments.Doc
import com.twoeightnine.root.xvii.utils.ApiUtils
import com.twoeightnine.root.xvii.utils.DownloadUtils
import com.twoeightnine.root.xvii.utils.PermissionHelper
import global.msnthrp.xvii.uikit.extensions.applyBottomInsetPadding
import global.msnthrp.xvii.uikit.extensions.applyTopInsetPadding
import global.msnthrp.xvii.uikit.extensions.toggle
import global.msnthrp.xvii.uikit.utils.GlideApp
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

        GlideApp.with(ivGif)
                .load(url)
                .into(ivGif)

        setStatusBarLight(isLight = false)
        rlTop.applyTopInsetPadding()
        rlBottom.applyBottomInsetPadding()
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