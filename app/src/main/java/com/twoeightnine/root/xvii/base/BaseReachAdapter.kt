package com.twoeightnine.root.xvii.base

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.utils.stylize
import kotlinx.android.synthetic.main.item_loader.view.*

/**
 * provides pagination functionality
 * [loader] is being invoked when there is a need to load more items. Int param is an offset
 *
 * usage:
 *  - [BaseReachAdapter.startLoading]
 *  - // load smth
 *  - [BaseReachAdapter.update] or [BaseReachAdapter.stopLoading]
 *  - on reload call [BaseReachAdapter.resetDone]
 */
abstract class BaseReachAdapter<T : Any, VH : RecyclerView.ViewHolder> constructor(
        context: Context,
        private var loader: (Int) -> Unit
) : BaseMultiSelectAdapter<T, RecyclerView.ViewHolder>(context) {

    var prevState: State = State.INITIAL
        private set

    var state: State = State.INITIAL
        private set

    private lateinit var stubLoadItem: T
    private var stackFromEnd = false

    abstract fun createHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder

    abstract fun bind(holder: VH, item: T)

    abstract fun createStubLoadItem(): T

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        stubLoadItem = createStubLoadItem()
        stackFromEnd = (recyclerView.layoutManager
                as? LinearLayoutManager)?.stackFromEnd == true
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (stackFromEnd && dy >= 0
                        || !stackFromEnd && dy <= 0) return


                val total = itemCount
                val thresholdPassed = if (stackFromEnd) {
                    val first = firstVisiblePosition(recyclerView.layoutManager)
                    first <= THRESHOLD
                } else {
                    val last = lastVisiblePosition(recyclerView.layoutManager)
                    last >= total - THRESHOLD
                }
                val rightState = state in arrayListOf(State.INITIAL, State.USUAL)
                if (rightState && thresholdPassed) {
                    startLoading(true)
                    loader.invoke(total)
                }
            }
        })
    }

    override fun clear() {
        super.clear()
        switchStates(State.INITIAL)
    }

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ) =
            when (viewType) {
                STUB_LOAD -> LoaderViewHolder(inflater.inflate(R.layout.item_loader, parent, false))
                else -> createHolder(parent, viewType)
            }

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        if (!isStubLoad(item)) {
            bind(holder as VH, item)
        }
    }

    override fun getItemViewType(position: Int) = when {
        isStubLoad(items[position]) -> STUB_LOAD
        else -> NO_STUB
    }

    private fun isStubLoad(obj: T) = stubLoadItem === obj

    /**
     * adds loader to RecyclerView
     */
    fun startLoading(addLoader: Boolean = false) {
        switchStates(State.LOADING)
        if (addLoader) {
            if (stackFromEnd) {
                add(stubLoadItem, 0)
            } else {
                add(stubLoadItem)
            }
        }
    }

    fun stopLoading(finished: Boolean = false) {
        remove(stubLoadItem)
        switchStates(if (finished) State.FINISHED else State.USUAL)
    }

    /**
     * restarts loading
     */
    fun reset() {
        switchStates(State.INITIAL)
    }

    /**
     * manipulates the states
     * pass empty list to stop loading
     */
    override fun update(items: List<T>) {
        remove(stubLoadItem)
        val hasChanges = itemCount != items.size
        super.update(items)
        val newState = when {
            hasChanges || prevState == State.INITIAL -> State.USUAL
            else -> State.FINISHED
        }
        switchStates(newState)
    }

    private fun switchStates(newState: State) {
        prevState = state
        state = newState
    }

    inner class LoaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        init {
            itemView.progressBar.stylize()
        }
    }

    enum class State {
        INITIAL,
        USUAL,
        LOADING,
        FINISHED
    }

    companion object {

        const val THRESHOLD = 5

        const val STUB_LOAD = 134
        const val NO_STUB = 135
    }
}