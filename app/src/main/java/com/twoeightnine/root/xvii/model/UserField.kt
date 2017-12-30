package com.twoeightnine.root.xvii.model

import android.view.View

/**
 * Created by root on 3/20/17.
 */

class UserField(var title: String,
                var value: String,
                var onClick: ((View) -> Unit)?,
                var onLongClick: ((View) -> Boolean)?)
