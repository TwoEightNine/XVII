package com.twoeightnine.root.xvii.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.twoeightnine.root.xvii.activities.RootActivity

abstract class BaseFragment : Fragment() {

    abstract fun getLayoutId(): Int

    protected val rootActivity
        get() = activity as? RootActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?) = View.inflate(activity, getLayoutId(), null)
}