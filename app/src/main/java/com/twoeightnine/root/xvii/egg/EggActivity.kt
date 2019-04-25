package com.twoeightnine.root.xvii.egg

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.activities.ContentActivity

class EggActivity : ContentActivity() {

    override fun getLayoutId() = R.layout.activity_content

    override fun getFragment(args: Bundle?) = EggFragment.newInstance(
            args?.getInt(EggFragment.ARG_MODE) ?: EggFragment.MODE_FIGHT_CLUB
    )

    companion object {
        fun launch(context: Context?, mode: Int = EggFragment.MODE_FIGHT_CLUB) {
            context?.startActivity(Intent(context, EggActivity::class.java).apply {
                putExtra(EggFragment.ARG_MODE, mode)
            })
        }
    }
}