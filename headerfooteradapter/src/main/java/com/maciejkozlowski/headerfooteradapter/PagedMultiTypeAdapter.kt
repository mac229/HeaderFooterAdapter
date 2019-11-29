package com.maciejkozlowski.headerfooteradapter

import android.view.ViewGroup
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import me.drakeet.multitype.MultiTypeAdapter

/**
 * Created by Maciej Kozłowski on 05.04.2018.
 */
abstract class PagedMultiTypeAdapter<ITEM>(
        private val multiTypeAdapter: MultiTypeAdapter,
        diffItemCallback: DiffUtil.ItemCallback<ITEM>
) : PagedListAdapter<ITEM, RecyclerView.ViewHolder>(diffItemCallback) {

    override fun getItemViewType(position: Int): Int {
        return multiTypeAdapter.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return multiTypeAdapter.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        getItem(position) // Workaround for paging library
        multiTypeAdapter.onBindViewHolder(holder, position)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        getItem(position) // Workaround for paging library
        multiTypeAdapter.onBindViewHolder(holder, position, payloads)
    }

    override fun submitList(pagedList: PagedList<ITEM>?) {
        super.submitList(pagedList)
        if (pagedList == null) {
            multiTypeAdapter.items = emptyList<ITEM>()
        } else {
            multiTypeAdapter.items = pagedList
        }
    }
}
