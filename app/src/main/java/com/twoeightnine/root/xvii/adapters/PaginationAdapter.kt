package com.twoeightnine.root.xvii.adapters

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import butterknife.BindView
import butterknife.ButterKnife
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.adapters.BaseAdapter


abstract class PaginationAdapter<T> @JvmOverloads constructor(context: Context,
                                                              protected var loader: (Int) -> Unit,
                                                              private val offsetSize: Int = PaginationAdapter.OFFSET_SIZE) : BaseAdapter<T, androidx.recyclerview.widget.RecyclerView.ViewHolder>(context) {
    protected var isLoaderAdded: Boolean = false
    protected var isTrierAdded: Boolean = false

    var trier: (() -> Unit)? = null


    var isLoading: Boolean = false
        protected set
    var isDone: Boolean = false
    protected var layoutManager: androidx.recyclerview.widget.RecyclerView.LayoutManager? = null

    override fun onAttachedToRecyclerView(recyclerView: androidx.recyclerview.widget.RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        layoutManager = recyclerView.layoutManager
        recyclerView.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy <= 0)
                    return

                val total = itemCount
                val last = lastVisiblePosition()
                if (!isDone && !isLoading && last >= total - THRESHOLD) {
                    loader.invoke(total)
                    startLoading()
                }


            }
        })

    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int) =
        when (viewType) {
            STUB_LOAD -> LoaderViewHolder(inflater.inflate(R.layout.item_loader, parent, false))
            STUB_TRY -> TryViewHolder(inflater.inflate(R.layout.item_try_again, parent, false))
            else -> createHolder(parent, viewType)
        }

    abstract fun createHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder

    open var stubLoadItem: T? = null
        get() = null

    open fun isStubLoad(obj: T): Boolean {
        return false
    }

    open var stubTryItem: T? = null
        get() = null

    open fun isStubTry(obj: T): Boolean {
        return false
    }

    override fun getItemViewType(position: Int) =
        if (isStubLoad(items[position])) {
            STUB_LOAD
        } else if (isStubTry(items[position])) {
            STUB_TRY
        } else {
            NOSTUB
        }


    fun lastVisiblePosition() =
        if (layoutManager != null) {
            (layoutManager as androidx.recyclerview.widget.LinearLayoutManager).findLastVisibleItemPosition()
        } else {
            -1
        }


    fun firstVisiblePosition() =
        if (layoutManager != null) {
            (layoutManager as androidx.recyclerview.widget.LinearLayoutManager).findFirstVisibleItemPosition()
        } else {
            -1
        }

    fun startLoading() {
        isLoading = true
        removeStubTry()
        addStubLoad()
    }

    fun setErrorLoading() {
        stopLoading()
        addStubTry()
    }

    open fun addStubLoad() {
        if (!isLoaderAdded) {
            add(stubLoadItem as T)
            isLoaderAdded = true
        }
    }

    open fun addStubTry() {
        if (!isTrierAdded) {
            add(stubTryItem as T)
            isTrierAdded = true
        }
    }

    @JvmOverloads fun stopLoading(items: MutableList<T>?, toTop: Boolean = false) {
        isLoading = false
        if (items != null && items.size == 0) {
            isDone = true
        }
        removeStubLoad()
        if (toTop) {
            for (i in items!!.indices) {
                add(items[i], 0)
            }
        } else {
            addAll(items!!)
        }
    }

    fun removeStub() {
        removeStubLoad()
        removeStubTry()
    }

    fun removeStubLoad() {
        remove(stubLoadItem as T)
        isLoaderAdded = false
    }

    fun removeStubTry() {
        remove(stubTryItem as T)
        isTrierAdded = false
    }

    fun stopLoading() {
        isLoading = false
        removeStubLoad()
    }


    inner class LoaderViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    inner class TryViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {

        @BindView(R.id.llTryAgain)
        lateinit var llTryAgain: LinearLayout

        init {
            ButterKnife.bind(this, view)
            llTryAgain.setOnClickListener { trier?.invoke() }
        }
    }

    companion object {

        var START_OFFSET = 0
        var OFFSET_SIZE = 20
        var THRESHOLD = 5

        var STUB_LOAD = 134
        var STUB_TRY = 136
        var NOSTUB = 135
    }
}
