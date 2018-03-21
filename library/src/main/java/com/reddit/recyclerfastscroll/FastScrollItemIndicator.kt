package com.reddit.recyclerfastscroll

import android.support.annotation.DrawableRes

sealed class FastScrollItemIndicator {
  data class Icon(@DrawableRes val iconRes: Int) : FastScrollItemIndicator()
  data class Text(val text: String) : FastScrollItemIndicator()
}
