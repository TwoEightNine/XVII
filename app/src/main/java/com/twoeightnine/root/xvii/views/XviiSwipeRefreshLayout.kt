package com.twoeightnine.root.xvii.views

import android.content.Context
import android.util.AttributeSet
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout
import com.twoeightnine.root.xvii.managers.Prefs

class XviiSwipeRefreshLayout : SwipyRefreshLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet)

    init {
        setDistanceToTriggerSync(100)
        if (Prefs.isLightTheme) {
            setColorSchemeColors(Prefs.color)
        }
    }

}