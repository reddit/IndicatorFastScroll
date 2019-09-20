package com.reddit.indicatorfastscroll

import android.view.View
import androidx.annotation.StyleRes

internal fun View.throwIfMissingAttrs(@StyleRes styleRes: Int, block: () -> Unit) {
  try {
    block()
  } catch (e: IllegalArgumentException) {
    throw IllegalArgumentException(
      "This ${this::class.java.simpleName} is missing an attribute. " +
        "Add it to its style, or make the style inherit from " +
        "${resources.getResourceName(styleRes)}.",
      e
    )
  }
}
