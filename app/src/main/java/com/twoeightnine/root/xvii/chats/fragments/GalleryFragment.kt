package com.twoeightnine.root.xvii.chats.fragments

import android.provider.MediaStore
import android.provider.MediaStore.MediaColumns
import android.support.design.widget.FloatingActionButton
import android.view.View
import android.widget.GridView
import butterknife.BindView
import butterknife.ButterKnife
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.SimpleAdapter
import com.twoeightnine.root.xvii.chats.Titleable
import com.twoeightnine.root.xvii.chats.adapters.GalleryAdapter
import com.twoeightnine.root.xvii.fragments.BaseFragment
import com.twoeightnine.root.xvii.managers.Lg
import com.twoeightnine.root.xvii.managers.Style

class GalleryFragment: BaseFragment(), Titleable, SimpleAdapter.OnMultiSelected {

    companion object {
        fun newInstance(listener: ((MutableList<String>) -> Unit)?): GalleryFragment {
            val frag = GalleryFragment()
            frag.listener = listener
            return frag
        }
    }

    override fun getTitle() = getString(R.string.gallery)

    @BindView(R.id.gvGallery)
    lateinit var gvGallery: GridView
    @BindView(R.id.fabDone)
    lateinit var fabDone: FloatingActionButton

    lateinit var adapter: GalleryAdapter

    var listener: ((MutableList<String>) -> Unit)? = null

    override fun bindViews(view: View) {
        super.bindViews(view)
        ButterKnife.bind(this, view)
        initAdapter()
        Style.forFAB(fabDone)
    }

    fun initAdapter() {
        adapter = GalleryAdapter({}, {})
        gvGallery.adapter = adapter
        adapter.add(getAllShownImagesPath())
        fabDone.setOnClickListener {
            listener?.invoke(adapter.multiSelectRaw)
        }
        fabDone.hide()
        adapter.multiListener = this
        gvGallery.setOnItemClickListener {
            _, _, pos, _ ->
            val path = adapter.items[pos]
            adapter.multiSelect(path)
            adapter.notifyDataSetChanged()
        }
    }

    override fun onNonEmpty() {
        fabDone.show()
    }

    override fun onEmpty() {
        fabDone.hide()
    }

    private fun getAllShownImagesPath(): MutableList<String> {
        val listOfAllImages: MutableList<String> = mutableListOf()
        val uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaColumns.DATE_MODIFIED)
        val cursor = activity.contentResolver.query(uri, projection, null, null, "${MediaColumns.DATE_MODIFIED} DESC")
        val columnIndexData = cursor.getColumnIndexOrThrow(MediaColumns.DATA)

        while (cursor.moveToNext()) {
            val absolutePathOfImage = cursor.getString(columnIndexData)
            if (absolutePathOfImage != null) {
                listOfAllImages.add(absolutePathOfImage)
            } else {
                Lg.wtf("gallery open: string is null")
            }
        }
        cursor.close()
        return listOfAllImages
    }

    override fun getLayout() = R.layout.fragment_gallery
}