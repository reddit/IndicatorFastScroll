package com.reddit.recyclerfastscroll

import android.content.Context
import android.content.res.ColorStateList
import android.support.annotation.AttrRes
import android.support.annotation.DimenRes
import android.support.v4.content.ContextCompat
import android.view.View
import androidx.content.res.getResourceIdOrThrow
import androidx.content.res.use
import java.lang.IllegalArgumentException

internal fun Context.getColorFromAttr(@AttrRes attrId: Int): Int {
  return ContextCompat.getColor(this, getResId(attrId))
}

internal fun Context.getColorStateListFromAttr(@AttrRes attrId: Int): ColorStateList {
  return ContextCompat.getColorStateList(this, getResId(attrId))!!
}

internal fun Context.getResId(@AttrRes attr: Int): Int {
  return theme.obtainStyledAttributes(intArrayOf(attr)).use {
    it.getResourceIdOrThrow(0)
  }
}

internal fun Context.getDimenPixelSize(@DimenRes dimen: Int): Int {
  return resources.getDimensionPixelSize(dimen)
}

internal fun View.throwIfMissingAttrs(friendlyStyleName: String, block: () -> Unit) {
  try {
    block()
  } catch (e: IllegalArgumentException) {
    throw IllegalArgumentException(
        "This ${this::class.java.simpleName} is missing an attribute. " +
            "Add it to its style, or make the style inherit from $friendlyStyleName.",
        e
    )
  }
}
