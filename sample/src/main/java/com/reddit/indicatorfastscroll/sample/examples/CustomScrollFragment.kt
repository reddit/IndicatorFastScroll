package com.reddit.indicatorfastscroll.sample.examples

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearSmoothScroller
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.reddit.indicatorfastscroll.sample.ListItem
import com.reddit.indicatorfastscroll.sample.R
import com.reddit.indicatorfastscroll.sample.SAMPLE_DATA_TEXT
import com.reddit.indicatorfastscroll.sample.SampleAdapter
import com.reddit.recyclerfastscroll.FastScrollItemIndicator
import com.reddit.recyclerfastscroll.FastScrollThumbView
import com.reddit.recyclerfastscroll.FastScrollView

class CustomScrollFragment : Fragment() {

  private lateinit var recyclerView: RecyclerView
  private lateinit var fastScrollView: FastScrollView
  private lateinit var fastScrollThumbView: FastScrollThumbView

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View {
    val view = inflater.inflate(R.layout.sample_basic, container, false)

    val data = listOf(ListItem.DataItem(
        "Items will be scrolled to the top!",
        showInFastScroll = false
    )) + SAMPLE_DATA_TEXT

    recyclerView = view.findViewById(R.id.sample_basic_recyclerview)
    val linearLayoutManager = LinearLayoutManager(context)
    recyclerView.apply {
      layoutManager = linearLayoutManager
      adapter = SampleAdapter(data)
    }

    fastScrollView = view.findViewById(R.id.sample_basic_fastscroll)
    fastScrollView.apply {
      setupWithRecyclerView(
          recyclerView,
          { position ->
            data[position]
                .takeIf(ListItem::showInFastScroll)
                ?.let { item ->
                  FastScrollItemIndicator.Text(
                      item
                          .title
                          .substring(0, 1)
                          .toUpperCase()
                  )
                }
          },
          useDefaultScroller = false
      )
      val smoothScroller: LinearSmoothScroller = object : LinearSmoothScroller(context) {
        override fun getVerticalSnapPreference(): Int = SNAP_TO_START
      }
      itemIndicatorSelectedCallbacks += object : FastScrollView.ItemIndicatorSelectedCallback {
        override fun onItemIndicatorSelected(
            indicator: FastScrollItemIndicator,
            indicatorCenterY: Int,
            itemPosition: Int
        ) {
          recyclerView.stopScroll()
          smoothScroller.targetPosition = itemPosition
          linearLayoutManager.startSmoothScroll(smoothScroller)
        }
      }
    }

    fastScrollThumbView = view.findViewById(R.id.sample_basic_fastscroll_thumb)
    fastScrollThumbView.apply {
      setupWithFastScrollView(fastScrollView)
    }

    return view
  }

}
