package com.twoeightnine.root.xvii.chats.attachments.gallery

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.chats.attachments.base.BaseAttachViewModel
import com.twoeightnine.root.xvii.chats.attachments.gallery.model.DeviceItem
import com.twoeightnine.root.xvii.lg.Lg
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.utils.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_gallery_new.*
import java.io.File
import javax.inject.Inject

class GalleryFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: BaseAttachViewModel.Factory
    private lateinit var viewModel: GalleryViewModel

    private val imageUtils by lazy {
        ImageUtils(activity ?: throw IllegalStateException("Where is activity?"))
    }

    private val adapter by lazy {
        GalleryAdapter(contextOrThrow, ::onCameraClick, ::loadMore)
    }

    private val permissionHelper by lazy {
        PermissionHelper(this)
    }

    override fun getLayoutId() = R.layout.fragment_gallery_new

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        App.appComponent?.inject(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory)[GalleryViewModel::class.java]
        initRecycler()
        viewModel.getAttach().observe(this, Observer { updateList(it) })
        reloadData()
        adapter.startLoading()

        progressBar.show()
        swipeRefresh.setOnRefreshListener { reloadData() }

        with(fabDone) {
            setOnClickListener {
                selectedSubject.onNext(adapter.multiSelect)
                adapter.clearMultiSelect()
            }
            stylize()
        }
        rlPermissions.setVisible(!permissionHelper.hasStoragePermissions())
        rlPermissions.setOnClickListener {
            permissionHelper.request(arrayOf(PermissionHelper.READ_STORAGE, PermissionHelper.WRITE_STORAGE)) {
                rlPermissions.hide()
                progressBar.show()
                reloadData()
            }
        }
        progressBar.stylize()

        btnCamera.setOnClickListener {
            imageUtils.dispatchTakePictureIntent(this)
        }
        btnDoc.setOnClickListener {
            imageUtils.dispatchSelectFile(this)
        }
        btnDoc.stylize()
        btnCamera.stylize()
    }

    private fun reloadData() {
        if (permissionHelper.hasStoragePermissions()) {
            adapter.reset()
            adapter.startLoading()
            viewModel.loadAttach()
        } else {
            rlPermissions.show()
            progressBar.hide()
        }
    }

    private fun updateList(data: Wrapper<ArrayList<DeviceItem>>) {
        swipeRefresh.isRefreshing = false
        progressBar.hide()
        if (data.data != null) {
            adapter.update(data.data)
        } else {
            showError(context, data.error ?: getString(R.string.error))
        }
    }

    private fun loadMore(offset: Int) {
        viewModel.loadAttach(offset)
    }

    private fun initRecycler() {
        rvAttachments.layoutManager = GridLayoutManager(context, SPAN_COUNT)
        rvAttachments.adapter = adapter
        adapter.multiSelectMode = true
        adapter.multiListener = fabDone::setVisible
    }

    private fun onCameraClick() {

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_CANCELED) return

        val path = imageUtils.getPath(requestCode, data)
        if (path != null && File(path).length() != 0L) {
            val type = if (requestCode == ImageUtils.REQUEST_SELECT_FILE) {
                DeviceItem.Type.DOC
            } else {
                DeviceItem.Type.PHOTO
            }
            selectedSubject.onNext(arrayListOf(DeviceItem(time() * 1000L, path, type)))
            adapter.clearMultiSelect()
        } else {
            Lg.wtf("[camera] path is empty but request code is $requestCode and data = $data")
        }
    }

    companion object {
        const val SPAN_COUNT = 4

        private val disposables = CompositeDisposable()
        private val selectedSubject = PublishSubject.create<List<DeviceItem>>()

        fun newInstance(onSelected: (List<DeviceItem>) -> Unit): GalleryFragment {
            selectedSubject.subscribe(onSelected).let { disposables.add(it) }
            return GalleryFragment()
        }

        fun clear() {
            disposables.clear()
        }
    }
}