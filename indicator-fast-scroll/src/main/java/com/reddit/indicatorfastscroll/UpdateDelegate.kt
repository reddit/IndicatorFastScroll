package com.reddit.indicatorfastscroll

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal class UpdateDelegate<T>(val update: () -> Unit) : ReadWriteProperty<Any?, T> {

  var set = false
  var value: T? = null

  override fun getValue(thisRef: Any?, property: KProperty<*>): T {
    if (!set) {
      throw IllegalStateException("Property ${property.name} should be initialized before get.")
    }
    @Suppress("UNCHECKED_CAST")
    return value as T
  }

  override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
    val wasSet = set
    this.set = true
    this.value = value
    if (wasSet) {
      update()
    }
  }

}

/**
 * A delegate that sets a backing value and calls [update] on every change after the first.
 */
internal fun <T> onUpdate(update: () -> Unit) = UpdateDelegate<T>(update)
