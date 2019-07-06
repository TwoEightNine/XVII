package com.twoeightnine.root.xvii.egg

import android.os.Bundle
import android.text.Html
import android.view.View
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.utils.load
import com.twoeightnine.root.xvii.utils.screenWidth
import com.twoeightnine.root.xvii.utils.show
import kotlinx.android.synthetic.main.fragment_egg.*

/**
 * Created by chuck palahniuk on 8/18/17.
 */

class EggFragment : BaseFragment() {

    private val mode by lazy { arguments?.getInt(ARG_MODE) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when (mode) {
            MODE_FIGHT_CLUB -> bindFightClub()
            MODE_LETOV_AGAINST -> bindLetovAgainst()
        }
    }

    private fun bindFightClub() {
        rlFightClub.show()
        val scale = 500f / 335f
        val width = screenWidth(activity ?: return)
        val height = (width / scale).toInt()
        ivBack.load(URL_FIGHT_CLUB, placeholder = false) {
            resize(width, height)
            centerCrop()
        }
        tvText.text = Html.fromHtml(getString(R.string.quote))
    }

    private fun bindLetovAgainst() {
        rlLetovAgainst.show()
        ivLetov.load(URL_LETOV_AGAINST, placeholder = false) {
            fit()
        }
    }

    override fun getLayoutId() = R.layout.fragment_egg

    companion object {

        const val ARG_MODE = "mode"
        const val MODE_FIGHT_CLUB = 1
        const val MODE_LETOV_AGAINST = 2

        const val URL_LETOV_AGAINST = "https://s00.yaplakal.com/pics/pics_original/4/7/2/6998274.jpg"
        const val URL_FIGHT_CLUB = "https://s-media-cache-ak0.pinimg.com/originals/22/6e/f8/226ef80405ed0da0b726c13d4a0bc9a1.jpg"

        fun newInstance(mode: Int = MODE_FIGHT_CLUB): EggFragment {
            val fragment = EggFragment()
            fragment.arguments = Bundle().apply {
                putInt(ARG_MODE, mode)
            }
            return fragment
        }
    }
}