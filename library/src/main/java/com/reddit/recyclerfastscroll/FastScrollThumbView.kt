package com.reddit.recyclerfastscroll

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.support.animation.DynamicAnimation
import android.support.animation.SpringAnimation
import android.support.animation.SpringForce
import android.support.constraint.ConstraintLayout
import android.support.v4.widget.TextViewCompat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.content.res.getColorOrThrow
import androidx.content.res.getColorStateListOrThrow
import androidx.content.res.getResourceIdOrThrow
import androidx.content.res.use
import androidx.view.doOnPreDraw
import androidx.view.isVisible
import com.reddit.indicatorfastscroll.R
import kotlin.properties.Delegates

/**
 * Companion view for a [fast scroller][FastScrollView] that shows its currently pressed indicator
 * in a small bubble near the user's finger.
 * This view should be vertically aligned with its fast scroller; its top and bottom should be
 * the same, though they don't necessarily need to have the same parent view. Horizontal placement
 * is independent of the fast scroller's.
 * A FastScrollThumbView is not required for a fast scroller to work, but it provides an
 * out-of-the-box solution for visible feedback.
 *
 * @see setupWithFastScrollView
 * @see FastScrollView
 */
class FastScrollThumbView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.recyclerFastScrollThumbStyle
) : ConstraintLayout(
    context,
    attrs,
    defStyleAttr
), FastScrollView.ItemIndicatorSelectedCallback {

  private var thumbColor: ColorStateList by Delegates.notNull()
  private var iconColor: Int by Delegates.notNull()
  private var textAppearanceRes: Int by Delegates.notNull()
  private var textColor: Int by Delegates.notNull()

  private val thumbView: ViewGroup
  private val textView: TextView
  private val iconView: ImageView

  private val isSetup: Boolean get() = (fastScrollView != null)
  private var fastScrollView: FastScrollView? = null

  private val thumbAnimation: SpringAnimation

  init {
    context.theme.obtainStyledAttributes(
        attrs,
        R.styleable.FastScrollThumbView,
        defStyleAttr,
        R.style.Widget_RecyclerFastScroll_FastScrollThumb
    ).use { attrsArray ->
      throwIfMissingAttrs(friendlyStyleName = "@style/Widget.RecyclerFastScroll.FastScrollThumb") {
        thumbColor = attrsArray.getColorStateListOrThrow(R.styleable.FastScrollThumbView_thumbColor)
        iconColor = attrsArray.getColorOrThrow(R.styleable.FastScrollThumbView_iconColor)
        textAppearanceRes = attrsArray.getResourceIdOrThrow(
            R.styleable.FastScrollThumbView_android_textAppearance
        )
        textColor = attrsArray.getColorOrThrow(R.styleable.FastScrollThumbView_android_textColor)
      }
    }

    LayoutInflater.from(context).inflate(R.layout.fast_scroll_thumb_view, this, true)
    thumbView = findViewById(R.id.fast_scroll_thumb)
    textView = thumbView.findViewById(R.id.fast_scroll_thumb_text)
    iconView = thumbView.findViewById(R.id.fast_scroll_thumb_icon)

    thumbView.stateListAnimator?.let { animator ->
      // Workaround for StateListAnimator not keeping its state in sync with its drawable pre-attach
      if (!thumbView.isAttachedToWindow) {
        thumbView.doOnPreDraw {
          animator.jumpToCurrentState()
        }
      }
    }
    thumbView.backgroundTintList = thumbColor
    if (Build.VERSION.SDK_INT == 21) {
      // Workaround for 21 background tint bug
      (thumbView.background as GradientDrawable).apply {
        mutate()
        color = thumbColor
      }
    }
    TextViewCompat.setTextAppearance(textView, textAppearanceRes)
    textView.setTextColor(textColor)
    iconView.imageTintList = ColorStateList.valueOf(iconColor)

    thumbAnimation = SpringAnimation(thumbView, DynamicAnimation.TRANSLATION_Y).apply {
      spring = SpringForce().apply {
        dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
      }
    }
  }

  /**
   * Sets up this [FastScrollThumbView] to show the currently pressed item indicator for
   * [fastScrollView]. It will follow the user's finger and is only visible when the fast scroller
   * is being used.
   * Only call this function once.
   *
   * @param fastScrollView the [FastScrollView] whose currently pressed indicator will be presented.
   */
  @SuppressLint("ClickableViewAccessibility")
  fun setupWithFastScrollView(fastScrollView: FastScrollView) {
    if (isSetup) throw IllegalStateException("Only set this view's FastScrollView once!")
    this.fastScrollView = fastScrollView

    fastScrollView.itemIndicatorSelectedCallbacks += this
    fastScrollView.setOnTouchListener { _, event ->
      if (event.action == MotionEvent.ACTION_DOWN) {
        isActivated = true
      } else if (event.action == MotionEvent.ACTION_UP) {
        isActivated = false
      }

      false
    }
  }

  override fun onItemIndicatorSelected(
      indicator: FastScrollItemIndicator,
      indicatorCenterY: Int,
      itemPosition: Int
  ) {
    val thumbTargetY = indicatorCenterY.toFloat() - (thumbView.measuredHeight / 2)
    thumbAnimation.animateToFinalPosition(thumbTargetY)

    when (indicator) {
      is FastScrollItemIndicator.Text -> {
        textView.isVisible = true
        iconView.isVisible = false

        textView.text = indicator.text
      }
      is FastScrollItemIndicator.Icon -> {
        textView.isVisible = false
        iconView.isVisible = true

        iconView.setImageResource(indicator.iconRes)
      }
    }
  }

}
