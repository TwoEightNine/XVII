package com.twoeightnine.root.xvii.picturer

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import butterknife.BindView
import butterknife.ButterKnife
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.dagger.ApiService
import com.twoeightnine.root.xvii.fragments.BaseFragment
import com.twoeightnine.root.xvii.model.Photo
import com.twoeightnine.root.xvii.utils.*
import com.twoeightnine.root.xvii.views.photoviewer.ImageViewerActivity
import javax.inject.Inject

class PicturerFragment : BaseFragment(), PicturerView {

    @Inject
    lateinit var api: ApiService

    @BindView(R.id.iv1)
    lateinit var iv1: ImageView
    @BindView(R.id.iv2)
    lateinit var iv2: ImageView
    @BindView(R.id.iv3)
    lateinit var iv3: ImageView
    @BindView(R.id.iv4)
    lateinit var iv4: ImageView
    @BindView(R.id.iv5)
    lateinit var iv5: ImageView
    @BindView(R.id.iv6)
    lateinit var iv6: ImageView

    @BindView(R.id.etGroup)
    lateinit var etGroup: EditText
    @BindView(R.id.btnGenerate)
    lateinit var btnGenerate: Button
    @BindView(R.id.checkbox)
    lateinit var checkbox: CheckBox

    private lateinit var presenter: PicturerPresenter

    private val photos = arrayOfNulls<Photo?>(6)

    private var selected = false

    override fun getLayout() = R.layout.fragment_picturer

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ButterKnife.bind(this, view)
        App.appComponent?.inject(this)
        presenter = PicturerPresenter(api)
        presenter.view = this

        iv1.setOnClickListener { onImageClick(0) }
        iv2.setOnClickListener { onImageClick(1) }
        iv3.setOnClickListener { onImageClick(2) }
        iv4.setOnClickListener { onImageClick(3) }
        iv5.setOnClickListener { onImageClick(4) }
        iv6.setOnClickListener { onImageClick(5) }

        btnGenerate.setOnClickListener { presenter.loadPictures(etGroup.text.toString().toInt()) }
    }

    override fun showLoading() {
        iv1.setImageResource(R.drawable.selector_rect)
        iv2.setImageResource(R.drawable.selector_rect)
        iv3.setImageResource(R.drawable.selector_rect)
        iv4.setImageResource(R.drawable.selector_rect)
        iv5.setImageResource(R.drawable.selector_rect)
        iv6.setImageResource(R.drawable.selector_rect)
    }

    override fun hideLoading() {
        iv1.setImageResource(R.drawable.placeholder)
        iv2.setImageResource(R.drawable.placeholder)
        iv3.setImageResource(R.drawable.placeholder)
        iv4.setImageResource(R.drawable.placeholder)
        iv5.setImageResource(R.drawable.placeholder)
        iv6.setImageResource(R.drawable.placeholder)
    }

    override fun showError(error: String) {
        showError(context, error)
    }

    override fun onImagesLoaded(images: List<Photo>) {
        images.forEachIndexed { index, photo ->
            photos[index] = photo
        }
//        showCommon(context, "Done!")
        selected = false
    }

    private fun showImages() {
        iv1.loadUrl(photos[0]?.optimalPhoto)
        iv2.loadUrl(photos[1]?.optimalPhoto)
        iv3.loadUrl(photos[2]?.optimalPhoto)
        iv4.loadUrl(photos[3]?.optimalPhoto)
        iv5.loadUrl(photos[4]?.optimalPhoto)
        iv6.loadUrl(photos[5]?.optimalPhoto)
    }

    private fun onImageClick(pos: Int) {
        if (selected) {
            ImageViewerActivity.viewImages(context, photos.map { it!! }.toMutableList(), pos)
        } else {
            val url = photos[pos]?.maxPhoto
            if (checkbox.isChecked && url != null) {
                downloadFile(context, url, getNameFromUrl(url), DownloadFileAsyncTask.PIC, {
                    showCommon(context, "Downloaded!")
                })
            }
            showImages()
            selected = true
        }
    }
}