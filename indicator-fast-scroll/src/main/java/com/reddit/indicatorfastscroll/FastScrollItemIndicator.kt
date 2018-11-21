package com.reddit.indicatorfastscroll

import androidx.annotation.DrawableRes

sealed class FastScrollItemIndicator {
  data class Icon(@DrawableRes val iconRes: Int) : FastScrollItemIndicator()
  data class Text(val text: String) : FastScrollItemIndicator()
}
