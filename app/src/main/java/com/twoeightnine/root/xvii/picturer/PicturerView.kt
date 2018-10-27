package com.twoeightnine.root.xvii.picturer

import com.twoeightnine.root.xvii.model.Photo
import com.twoeightnine.root.xvii.mvp.BaseView

interface PicturerView : BaseView {

    fun onImagesLoaded(images: List<Photo>)

}