package com.reddit.indicatorfastscroll

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.support.v4.widget.TextViewCompat
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.getColorStateListOrThrow
import androidx.core.content.res.getDimensionOrThrow
import androidx.core.content.res.getResourceIdOrThrow
import androidx.core.content.res.use
import androidx.core.view.children
import androidx.core.view.updatePadding
import kotlin.properties.Delegates

typealias ItemIndicatorWithPosition = Pair<FastScrollItemIndicator, Int>

/**
 * A fast scroller that observes a [RecyclerView]'s data and presents its items in a vertical column
 * of [text][FastScrollItemIndicator.Text] or [icon][FastScrollItemIndicator.Icon] indicators. It
 * also optionally handles scrolling to their respective items. It can be placed independently of
 * the RecyclerView, and has no layout dependencies.
 *
 * @see setupWithRecyclerView
 * @see FastScrollerThumbView
 */
class FastScrollerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.indicatorFastScrollerStyle,
    defStyleRes: Int = R.style.Widget_IndicatorFastScroll_FastScroller
) : LinearLayout(
    context,
    attrs,
    defStyleAttr,
    defStyleRes
) {

  private var iconColor: ColorStateList? = null
  private var textAppearanceRes: Int by Delegates.notNull()
  private var textColor: ColorStateList? = null
  private var textPadding: Float by Delegates.notNull()

  val itemIndicatorSelectedCallbacks: MutableList<ItemIndicatorSelectedCallback> = ArrayList()

  private val isSetup: Boolean get() = (recyclerView != null)
  private var recyclerView: RecyclerView? = null
  private lateinit var getItemIndicator: (Int) -> FastScrollItemIndicator?
  /**
   * An optional predicate for deciding which indicators to show after they have been computed.
   * The first parameter is the subject indicator.
   * The second parameter is its position in the [list of indicators][itemIndicators].
   * The third parameter is the total number of computed indicators, including ones that have been
   * filtered out via this predicate.
   * The function will be called when building the list of indicators, which happens after the
   * RecyclerView's adapter's data changes. It will be called on the UI thread.
   */
  var showIndicator: ((FastScrollItemIndicator, Int, Int) -> Boolean)? = null
    set(value) {
      field = value
      postUpdateItemIndicators()
    }

  /**
   * Whether or not the RecyclerView will be automatically scrolled when an indicator is pressed.
   * Set to false if you'd rather handle the scrolling yourself by adding a
   * [ItemIndicatorSelectedCallback].
   *
   * @see scrollToPosition
   */
  var useDefaultScroller: Boolean = true
  private var lastSelectedPosition: Int? = null
  private var isUpdateItemIndicatorsPosted = false

  private val itemIndicatorsWithPositions: MutableList<ItemIndicatorWithPosition> = ArrayList()
  /**
   * The list of indicators being shown. This will contain no duplicates, and will be built with
   * respect to the iteration order of the RecyclerView's adapter's data.
   */
  val itemIndicators: List<FastScrollItemIndicator>
    get() = itemIndicatorsWithPositions.map(ItemIndicatorWithPosition::first)

  init {
    context.theme.obtainStyledAttributes(
        attrs,
        R.styleable.FastScrollerView,
        defStyleAttr,
        defStyleRes
    ).use { attrsArray ->
      throwIfMissingAttrs(friendlyStyleName = "@style/Widget.IndicatorFastScroll.FastScroller") {
        iconColor = attrsArray.getColorStateListOrThrow(R.styleable.FastScrollerView_iconColor)
        textAppearanceRes = attrsArray.getResourceIdOrThrow(
            R.styleable.FastScrollerView_android_textAppearance
        )
        textColor = attrsArray.getColorStateListOrThrow(R.styleable.FastScrollerView_android_textColor)
        textPadding = attrsArray.getDimensionOrThrow(R.styleable.FastScrollerView_textPadding)
      }
    }

    isFocusableInTouchMode = true
    isClickable = true
    orientation = VERTICAL
    gravity = Gravity.CENTER


    if (isInEditMode) {
      itemIndicatorsWithPositions += listOf(
          ItemIndicatorWithPosition(FastScrollItemIndicator.Text("A"), 0),
          ItemIndicatorWithPosition(FastScrollItemIndicator.Text("B"), 1),
          ItemIndicatorWithPosition(FastScrollItemIndicator.Text("C"), 2),
          ItemIndicatorWithPosition(FastScrollItemIndicator.Text("D"), 3),
          ItemIndicatorWithPosition(FastScrollItemIndicator.Text("E"), 4)
      )
      bindItemIndicatorViews()
    }
  }

  /**
   * Sets up this [FastScrollerView] to present item indicators for [recyclerView]'s data.
   * The data is observed through its adapter, and each item is (optionally) mapped to an indicator
   * with [getItemIndicator]. After calling one of the adapter's notify methods,
   * [the list of indicators][itemIndicators] will be built and presented.
   * The indicators can optionally be filtered with [showIndicator].
   * Only call this function once.
   *
   * @param recyclerView the [RecyclerView] whose data's indicators will be presented.
   * @param getItemIndicator a function mapping an item position to a [FastScrollItemIndicator].
   *                         This will be called when building the list of indicators, which happens
   *                         immediately as well as whenever the adapter's data changes. If items
   *                         return identical indicators, they will be merged and only shown once.
   *                         To not show an indicator for an item, return null.
   *                         Called on the UI thread.
   * @param showIndicator an optional predicate for filtering indicators. This can be changed
   *                      (or removed) at any time.
   *                      See [FastScrollerView.showIndicator].
   * @param useDefaultScroller whether or not this FastScrollerView should automatically scroll
   *                           [recyclerView] when an indicator is pressed.
   *                           See [FastScrollerView.useDefaultScroller].
   */
  fun setupWithRecyclerView(
      recyclerView: RecyclerView,
      getItemIndicator: (Int) -> FastScrollItemIndicator?,
      showIndicator: ((FastScrollItemIndicator, Int, Int) -> Boolean)? = null,
      useDefaultScroller: Boolean = true
  ) {
    if (isSetup) throw IllegalStateException("Only set this view's RecyclerView once!")
    this.recyclerView = recyclerView
    this.getItemIndicator = getItemIndicator
    this.showIndicator = showIndicator
    this.useDefaultScroller = useDefaultScroller

    updateItemIndicators()
    recyclerView.adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
      override fun onChanged() {
        postUpdateItemIndicators()
      }

      override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) =
          onChanged()

      override fun onItemRangeInserted(positionStart: Int, itemCount: Int) =
          onChanged()

      override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) =
          onChanged()

      override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) =
          onChanged()
    })
  }

  private fun postUpdateItemIndicators() {
    if (!isUpdateItemIndicatorsPosted) {
      isUpdateItemIndicatorsPosted = true
      post {
        if (recyclerView!!.isAttachedToWindow) {
          updateItemIndicators()
        }
        isUpdateItemIndicatorsPosted = false
      }
    }
  }

  private fun updateItemIndicators() {
    itemIndicatorsWithPositions.clear()
    (0 until recyclerView!!.adapter.itemCount)
        .mapNotNull { position ->
          getItemIndicator(position)?.let { ItemIndicatorWithPosition(it, position) }
        }
        .distinctBy(ItemIndicatorWithPosition::first)
        .let { unfilteredIndicators ->
          showIndicator?.let {
            unfilteredIndicators.filterIndexed { index, (indicator, _) ->
              it(indicator, index, unfilteredIndicators.size)
            }
          } ?: unfilteredIndicators
        }
        .toCollection(itemIndicatorsWithPositions)

    bindItemIndicatorViews()
  }

  private fun bindItemIndicatorViews() {
    removeAllViews()

    fun createIconView(iconIndicator: FastScrollItemIndicator.Icon): ImageView =
        (LayoutInflater.from(context).inflate(
            R.layout.fast_scroller_indicator_icon, this, false
        ) as ImageView).apply {
          iconColor?.let(::setImageTintList)
          setImageResource(iconIndicator.iconRes)
          tag = iconIndicator
        }

    fun createTextView(textIndicators: List<FastScrollItemIndicator.Text>): TextView =
        (LayoutInflater.from(context).inflate(
            R.layout.fast_scroller_indicator_text, this, false
        ) as TextView).apply {
          TextViewCompat.setTextAppearance(this, textAppearanceRes)
          textColor?.let(::setTextColor)
          updatePadding(top = textPadding.toInt(), bottom = textPadding.toInt())
          setLineSpacing(textPadding, lineSpacingMultiplier)
          text = textIndicators.joinToString(separator = "\n") { it.text }
          tag = textIndicators
        }

    // Optimize the views by batching adjacent text indicators into a single TextView
    val viewCreators = ArrayList<() -> View>()
    itemIndicators.run {
      var index = 0
      while (index < lastIndex) {
        val textIndicatorsBatch = subList(index, size)
            .takeWhile { it is FastScrollItemIndicator.Text }
            // Limitation of type system, may be a better way
            .map { it as FastScrollItemIndicator.Text }
        if (textIndicatorsBatch.isNotEmpty()) {
          viewCreators.add { createTextView(textIndicatorsBatch) }
          index += textIndicatorsBatch.size
        } else {
          val indicator = this[index]
          when (indicator) {
            is FastScrollItemIndicator.Icon -> {
              viewCreators.add { createIconView(indicator) }
            }
            is FastScrollItemIndicator.Text -> {
              throw IllegalStateException("Text indicator wasn't batched")
            }
          }
          index++
        }
      }
    }
    viewCreators.forEach { createView ->
      addView(createView())
    }
  }

  private fun selectItemIndicator(
      indicator: FastScrollItemIndicator,
      indicatorCenterY: Int
  ) {
    val position = itemIndicatorsWithPositions
        .first { it.first == indicator }
        .let(ItemIndicatorWithPosition::second)
    if (position != lastSelectedPosition) {
      lastSelectedPosition = position
      if (useDefaultScroller) {
        scrollToPosition(position)
      }
      performHapticFeedback(
          // Semantically, dragging across the indicators is similar to moving a text handle
          if (Build.VERSION.SDK_INT >= 27) {
            HapticFeedbackConstants.TEXT_HANDLE_MOVE
          } else {
            HapticFeedbackConstants.KEYBOARD_TAP
          }
      )
      itemIndicatorSelectedCallbacks.forEach {
        it.onItemIndicatorSelected(indicator, indicatorCenterY, position)
      }
    }
  }

  private fun scrollToPosition(position: Int) {
    recyclerView!!.apply {
      stopScroll()
      smoothScrollToPosition(position)
    }
  }

  @SuppressLint("ClickableViewAccessibility")
  override fun onTouchEvent(event: MotionEvent): Boolean {
    fun View.containsY(y: Int) = y in (top until bottom)

    var consumed = super.onTouchEvent(event)

    if (event.action == MotionEvent.ACTION_UP) {
      lastSelectedPosition = null
      return consumed
    }

    val touchY = event.y.toInt()
    children.forEachIndexed { index, view ->
      if (view.containsY(touchY)) {
        when (view) {
          is ImageView -> {
            val touchedIndicator = view.tag as FastScrollItemIndicator.Icon
            val centerY = view.y.toInt() + (view.height / 2)
            selectItemIndicator(touchedIndicator, centerY)
            consumed = true
          }
          is TextView -> {
            @Suppress("UNCHECKED_CAST")
            val possibleTouchedIndicators = view.tag as List<FastScrollItemIndicator.Text>
            val textIndicatorsTouchY = touchY - view.top
            val textLineHeight = view.height / possibleTouchedIndicators.size
            val touchedIndicatorIndex = Math.min(
                textIndicatorsTouchY / textLineHeight,
                possibleTouchedIndicators.lastIndex
            )
            val touchedIndicator = possibleTouchedIndicators[touchedIndicatorIndex]

            val centerY = view.y.toInt() +
                (textLineHeight / 2) + (touchedIndicatorIndex * textLineHeight)
            selectItemIndicator(touchedIndicator, centerY)
            consumed = true
          }
        }
      }
    }

    return consumed;
  }

  interface ItemIndicatorSelectedCallback {
    fun onItemIndicatorSelected(
        indicator: FastScrollItemIndicator,
        indicatorCenterY: Int,
        itemPosition: Int
    )
  }

}
