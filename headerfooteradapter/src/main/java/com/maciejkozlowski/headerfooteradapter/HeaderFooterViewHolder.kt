package com.maciejkozlowski.headerfooteradapter

import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView

class HeaderFooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    internal val base = itemView as FrameLayout
}