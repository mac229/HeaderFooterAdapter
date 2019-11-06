package com.maciejkozlowski.headerfooteradapter

import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.FrameLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

abstract class HeaderFooterAdapter<VH : RecyclerView.ViewHolder, out T : RecyclerView.Adapter<VH>>(
    val innerAdapter: T
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 7898
        private const val TYPE_FOOTER = 7899
    }

    private val headers = ArrayList<View>()
    private val footers = ArrayList<View>()

    private var orientation: Int? = null

    init {
        this.innerAdapter.registerAdapterDataObserver(InternalAdapterDataObserver())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER || viewType == TYPE_FOOTER) {
            val frameLayout = FrameLayout(parent.context)
            frameLayout.layoutParams = getLayoutParams()
            HeaderFooterViewHolder(frameLayout)
        } else {
            innerAdapter.onCreateViewHolder(parent, viewType)
        }
    }

    private fun getLayoutParams(): LayoutParams {
        return when (orientation) {
            RecyclerView.VERTICAL   -> LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            RecyclerView.HORIZONTAL -> LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
            else                    -> LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        when {
            isHeader(position) -> bindHeader(position, holder)
            isFooter(position) -> bindFooter(position, holder)
            else               -> innerAdapter.onBindViewHolder(holder.cast(), position - headers.size, payloads)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when {
            isHeader(position) -> bindHeader(position, holder)
            isFooter(position) -> bindFooter(position, holder)
            else               -> innerAdapter.onBindViewHolder(holder.cast(), position - headers.size)
        }
    }

    private fun isHeader(position: Int): Boolean {
        return position < headers.size
    }

    private fun isFooter(position: Int): Boolean {
        return position >= headers.size + innerAdapter.itemCount
    }

    private fun bindFooter(position: Int, holder: RecyclerView.ViewHolder) {
        val view = footers[position - innerAdapter.itemCount - headers.size]
        prepareHeaderFooter(holder as HeaderFooterViewHolder, view)
    }

    private fun bindHeader(position: Int, holder: RecyclerView.ViewHolder) {
        val view = headers[position]
        prepareHeaderFooter(holder as HeaderFooterViewHolder, view)
    }

    private fun prepareHeaderFooter(vh: HeaderFooterViewHolder, view: View) {
        if (view.parent != null) {
            (view.parent as ViewGroup).removeView(view)
        }

        vh.base.removeAllViews()
        vh.base.addView(view)
    }

    override fun getItemCount(): Int {
        return headers.size + innerAdapter.itemCount + footers.size
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            isHeader(position) -> TYPE_HEADER
            isFooter(position) -> TYPE_FOOTER
            else               -> {
                val type = innerAdapter.getItemViewType(position - headers.size)
                if (type == TYPE_HEADER || type == TYPE_FOOTER) {
                    throw IllegalArgumentException("Item type cannot equal $TYPE_HEADER or $TYPE_FOOTER")
                }
                type
            }
        }
    }

    override fun setHasStableIds(hasStableIds: Boolean) {
        super.setHasStableIds(hasStableIds)
        innerAdapter.setHasStableIds(hasStableIds)
    }

    override fun getItemId(position: Int): Long {
        return when {
            isHeader(position) -> TYPE_HEADER.toLong()
            isFooter(position) -> TYPE_FOOTER.toLong()
            else               -> innerAdapter.getItemId(position)
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is HeaderFooterViewHolder) {
            super.onViewRecycled(holder)
        } else {
            innerAdapter.onViewRecycled(holder.cast())
        }
    }

    override fun onFailedToRecycleView(holder: RecyclerView.ViewHolder): Boolean {
        return if (holder is HeaderFooterViewHolder) {
            super.onFailedToRecycleView(holder)
        } else {
            innerAdapter.onFailedToRecycleView(holder.cast())
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        if (holder is HeaderFooterViewHolder) {
            super.onViewAttachedToWindow(holder)
            val params = holder.itemView.layoutParams
            if (params != null && params is StaggeredGridLayoutManager.LayoutParams) {
                params.isFullSpan = true
            }
        } else {
            innerAdapter.onViewAttachedToWindow(holder.cast())
        }
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        if (holder is HeaderFooterViewHolder) {
            super.onViewDetachedFromWindow(holder)
        } else {
            innerAdapter.onViewDetachedFromWindow(holder.cast())
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        orientation = getOrientation(recyclerView.layoutManager)
        innerAdapter.onAttachedToRecyclerView(recyclerView)
    }

    private fun getOrientation(layoutManager: RecyclerView.LayoutManager?): Int? {
        return when (layoutManager) {
            is LinearLayoutManager        -> layoutManager.orientation
            is GridLayoutManager          -> layoutManager.orientation
            is StaggeredGridLayoutManager -> layoutManager.orientation
            else                          -> null
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        innerAdapter.onDetachedFromRecyclerView(recyclerView)
    }

    fun addHeader(header: View) {
        if (!headers.contains(header)) {
            headers.add(header)
            notifyItemInserted(headers.size - 1)
        }
    }

    fun addHeaderAtBeginning(header: View) {
        if (!headers.contains(header)) {
            headers.add(0, header)
            notifyItemInserted(0)
        }
    }

    fun removeHeader(header: View) {
        if (headers.contains(header)) {
            notifyItemRemoved(headers.indexOf(header))
            headers.remove(header)
        }
    }

    fun addFooter(footer: View) {
        if (!footers.contains(footer)) {
            footers.add(footer)
            notifyItemInserted(headers.size + innerAdapter.itemCount + footers.size - 1)
        }
    }

    fun addFooterOnTop(footer: View) {
        if (!footers.contains(footer)) {
            footers.add(0, footer)
            notifyItemInserted(headers.size + innerAdapter.itemCount)
        }
    }

    fun removeFooter(footer: View) {
        if (footers.contains(footer)) {
            notifyItemRemoved(headers.size + innerAdapter.itemCount + footers.indexOf(footer))
            footers.remove(footer)
        }
    }

    fun clearHeaders() {
        val size = headers.size
        headers.clear()
        notifyItemRangeRemoved(0, size)

    }

    @Suppress("unchecked_cast")
    private fun RecyclerView.ViewHolder.cast(): VH {
        return this as VH
    }

    internal fun getAdapterPosition(positionOfInternalAdapter: Int): Int {
        return headers.size + positionOfInternalAdapter
    }

    private inner class InternalAdapterDataObserver : RecyclerView.AdapterDataObserver() {

        override fun onChanged() {
            notifyDataSetChanged()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            notifyItemRangeChanged(getAdapterPosition(positionStart), itemCount)
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
            notifyItemRangeChanged(getAdapterPosition(positionStart), itemCount)
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            notifyItemRangeInserted(getAdapterPosition(positionStart), itemCount)
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            notifyItemRangeRemoved(getAdapterPosition(positionStart), itemCount)
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            notifyItemMoved(getAdapterPosition(fromPosition), getAdapterPosition(toPosition))
        }
    }
}