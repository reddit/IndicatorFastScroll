package com.reddit.indicatorfastscroll

import androidx.recyclerview.widget.RecyclerView

internal class TestItemIndicatorsBuilder : ItemIndicatorsBuilder() {

  var timesBuildCalled = 0

  override fun buildItemIndicators(
      recyclerView: RecyclerView,
      getItemIndicator: (Int) -> FastScrollItemIndicator?,
      showIndicator: ((FastScrollItemIndicator, Int, Int) -> Boolean)?
  ): List<ItemIndicatorWithPosition> {
    timesBuildCalled++
    return super.buildItemIndicators(recyclerView, getItemIndicator, showIndicator)
  }

}
