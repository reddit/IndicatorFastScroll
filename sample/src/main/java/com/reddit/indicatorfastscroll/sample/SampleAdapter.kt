package com.reddit.indicatorfastscroll.sample

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.updatePaddingRelative
import androidx.recyclerview.widget.RecyclerView

class SampleAdapter(
    private val data: List<ListItem>
) : RecyclerView.Adapter<SampleAdapter.ViewHolder>() {

  private val containsHeaders: Boolean = data.any { it is ListItem.HeaderItem }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    return LayoutInflater.from(parent.context)
        .inflate(
            when (viewType) {
              VIEWTYPE_HEADER -> R.layout.header_item
              VIEWTYPE_DATA -> R.layout.data_item
              else -> throw IllegalArgumentException()
            },
            parent,
            false
        )
        .let(::ViewHolder)
  }

  override fun getItemCount() = data.count()

  override fun getItemViewType(position: Int): Int {
    return when (data[position]) {
      is ListItem.HeaderItem -> VIEWTYPE_HEADER
      is ListItem.DataItem -> VIEWTYPE_DATA
    }
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(data[position])
  }

  inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val titleView = itemView as TextView

    fun bind(listItem: ListItem) {
      when (listItem) {
        is ListItem.HeaderItem -> {
          titleView.text = listItem.title
          titleView.setCompoundDrawablesRelativeWithIntrinsicBounds(listItem.iconRes, 0, 0, 0)
          val iconColor = titleView.textColors
          if (Build.VERSION.SDK_INT >= 23) {
            titleView.compoundDrawableTintList = iconColor
          } else {
            titleView.compoundDrawablesRelative
                .filterNotNull()
                .forEach {
                  it.setTintList(iconColor)
                }
          }
        }
        is ListItem.DataItem -> {
          titleView.text = listItem.title
          if (containsHeaders) {
            titleView.updatePaddingRelative(start = titleView.context.resources.getDimensionPixelSize(
                R.dimen.list_with_headers_start_padding
            ))
          }
        }
      }
    }

  }

  companion object {
    const val VIEWTYPE_HEADER = 0
    const val VIEWTYPE_DATA = 1
  }

}
