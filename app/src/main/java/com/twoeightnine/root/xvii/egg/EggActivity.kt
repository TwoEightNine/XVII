package com.twoeightnine.root.xvii.egg

import android.content.Context
import android.content.Intent
import android.graphics.Color
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.ContentActivity

class EggActivity : ContentActivity() {

    override fun getLayoutId() = R.layout.activity_content

    override fun createFragment(intent: Intent?) = EggFragment.newInstance(
            intent?.extras?.getInt(EggFragment.ARG_MODE) ?: EggFragment.MODE_FIGHT_CLUB
    )

    override fun getNavigationBarColor() = Color.TRANSPARENT

    companion object {
        fun launch(context: Context?, mode: Int = EggFragment.MODE_FIGHT_CLUB) {
            context?.startActivity(Intent(context, EggActivity::class.java).apply {
                putExtra(EggFragment.ARG_MODE, mode)
            })
        }
    }
}