package com.twoeightnine.root.xvii.background.longpoll

import android.os.Bundle
import androidx.core.content.ContextCompat
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.activities.BaseActivity
import com.twoeightnine.root.xvii.utils.setBottomInsetPadding
import com.twoeightnine.root.xvii.utils.setTopInsetPadding
import kotlinx.android.synthetic.main.activity_explanation.*

class LongPollExplanationActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_explanation)
        tvTitle.setTopInsetPadding()
        svContent.setBottomInsetPadding()
    }

    override fun getStatusBarColor() = ContextCompat.getColor(this, R.color.status_bar)
}