package com.reddit.indicatorfastscroll.sample

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class HeaderSpacerItemDecoration(
    val getListItem: (Int) -> ListItem
) : RecyclerView.ItemDecoration() {

  override fun getItemOffsets(
      outRect: Rect,
      view: View,
      parent: RecyclerView,
      state: RecyclerView.State
  ) {
    val position = parent.getChildAdapterPosition(view)
    val listItem = getListItem(position)
    outRect.apply {
      when (listItem) {
        is ListItem.HeaderItem -> {
          top = if (position == 0) {
            0
          } else {
            view.context.resources.getDimensionPixelSize(R.dimen.header_middle_top_margin)
          }
        }
      }
      left = 0
      right = 0
      bottom = 0
    }
  }

}
