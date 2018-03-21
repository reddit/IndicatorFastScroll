package com.reddit.indicatorfastscroll.sample.examples

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.reddit.indicatorfastscroll.sample.*
import com.reddit.indicatorfastscroll.FastScrollItemIndicator
import com.reddit.indicatorfastscroll.FastScrollerThumbView
import com.reddit.indicatorfastscroll.FastScrollerView

class TextWithIconFragment : Fragment() {

  private lateinit var recyclerView: RecyclerView
  private lateinit var fastScrollerView: FastScrollerView
  private lateinit var fastScrollerThumbView: FastScrollerThumbView

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    val view = inflater.inflate(R.layout.sample_basic, container, false)

    val data = SAMPLE_DATA_TEXT_AND_HEADERS

    recyclerView = view.findViewById(R.id.sample_basic_recyclerview)
    recyclerView.apply {
      layoutManager = LinearLayoutManager(context)
      addItemDecoration(HeaderSpacerItemDecoration(data::get))
      adapter = SampleAdapter(data)
    }

    fastScrollerView = view.findViewById(R.id.sample_basic_fastscroller)
    fastScrollerView.apply {
      setupWithRecyclerView(
          recyclerView,
          { position ->
            data[position]
                .takeIf(ListItem::showInFastScroll)
                ?.let { item ->
                  when (item) {
                    is ListItem.HeaderItem -> FastScrollItemIndicator.Icon(item.iconRes)
                    is ListItem.DataItem ->
                      FastScrollItemIndicator.Text(
                          item
                              .title
                              .substring(0, 1)
                              .toUpperCase()
                      )
                  }
                }
          }
      )
    }

    fastScrollerThumbView = view.findViewById(R.id.sample_basic_fastscroller_thumb)
    fastScrollerThumbView.apply {
      setupWithFastScroller(fastScrollerView)
    }

    return view
  }

}
