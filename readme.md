<img align="right" src="../gh-pages/demo.gif" height="500px"/>

# Indicator Fast Scroll
###### by reddit

## Features

* Simple interface for expressing your data via fast scroll indicators
* Shows text and icons
* Works with standard RecyclerViews and in any layout
* Supports styling
* Haptic feedback
* Kotlin ♥

## Usage

Add the dependency to your app's `build.gradle`:
##### AndroidX:
```groovy
implementation 'com.reddit:indicator-fast-scroll:1.3.0'
```
##### Pre-AndroidX (older projects):
```groovy
implementation 'com.reddit:indicator-fast-scroll:1.0.1'
```

Then, add the fast scroller to the layout with your RecyclerView:
```xml
<com.reddit.indicatorfastscroll.FastScrollerView
    android:id="@+id/fastscroller"
    android:layout_width="32dp"
    android:layout_height="match_parent"
    />
```
Typically, you'll want to place the fast scroller on the right edge of your RecyclerView, but it can be placed anywhere.

When setting up your views, call `setupWithRecyclerView()` to wire up the FastScrollerView to your RecyclerView.
##### Kotlin:
```kotlin
fastScrollerView.setupWithRecyclerView(
    recyclerView,
    { position ->
      val item = data[position] // Get your model object
                                // or fetch the section at [position] from your database
      FastScrollItemIndicator.Text(
          item.title.substring(0, 1).toUpperCase() // Grab the first letter and capitalize it
      ) // Return a text indicator
    }
)
```
##### Java:
```java
fastScrollerView.setupWithRecyclerView(
    recyclerView,
    (position) -> {
        ItemModel item = data.get(position); // Get your model object
                                             // or fetch the section at [position] from your database
        return new FastScrollItemIndicator.Text(
            item.getTitle().substring(0, 1).toUpperCase() // Grab the first letter and capitalize it
        ); // Return a text indicator
    }
);
```
🎉 That's all for basic setup! Your list should now have a working fast scroller.

There's also an optional companion view that shows a hovering "thumb" next to the user's finger when the fast scroller is being used. To use it, add it to your layout:
```xml
<com.reddit.indicatorfastscroll.FastScrollerThumbView
    android:id="@+id/fastscroller_thumb"
    android:layout_width="40dp"
    android:layout_height="match_parent"
    android:layout_alignBottom="@+id/fastscroller"
    android:layout_alignTop="@+id/fastscroller"
    android:layout_marginEnd="16dp"
    android:layout_toStartOf="@+id/fastscroller"
    />
```
It should be placed alongside the fast scroller. The exact layout hierarchy doesn't matter, but the thumb view's top and bottom should be aligned with the fast scroller's.

Then, set up the thumb:
```kotlin
fastScrollerThumbView.setupWithFastScroller(fastScrollerView)
```

## Advanced usage

### Mapping positions to indicators
When setting up the fast scroller with your RecyclerView, you have to provide a mapping function that returns the fast scroll indicator that's desired (if any) to represent a section in the list.

In this function, return a `Text` if you want this position's section to be represented by a text indicator (typically its starting letter for an alphabetical list), return an `Icon` if you want an icon, or return `null` if you don't want this section to be shown in the fast scroller.

Indicators in the fast scroller won't have any duplicates. You should return identical `FastScrollItemIndicator`s for each item in your list that belongs to the same section. This often happens implicitly; if your database model contains a `section` field and you create a `FastScrollItemIndicator.Text` based on it, all items with the same `section` will be grouped together in the fast scroller. Similarly, if a list is alphabetical, creating a `FastScrollItemIndicator.Text` with the first letter of each item's name will result in all items starting with the same letter to be grouped together.

The fast scroller observes your RecyclerView's adapter's data, and will call your mapping function for each list position whenever the data has changed. [Here's a sample](https://github.com/reddit/IndicatorFastScroll/blob/master/sample/src/main/java/com/reddit/indicatorfastscroll/sample/examples/TextWithIconFragment.kt#L35-L53).

### Styling
The fast scroller and thumb view support standard attributes that can be set in an XML layout or a custom style.

##### FastScrollerView
* `fastScrollerIconColor`: Color or ColorStateList for tinting icon indicators (supports `state_pressed="true"`)
* `android:textAppearance`: Text appearance for text indicators
* `android:textColor`: Color or ColorStateList for text indicators (supports `state_pressed="true"`)
* `fastScrollerTextPadding`: Dimension for vertical padding applied to text indicators

##### FastScrollerThumbView
* `fastScrollerThumbColor`: Color for the background of the thumb circle
* `fastScrollerIconColor`: Color for tinting the currently selected icon indicator
* `android:textAppearance`: Text appearance for the currently selected text indicator
* `android:textColor`: Color for the currently selected text indicator

These can be set in an XML layout, style, or theme. [Here's a sample](https://github.com/reddit/IndicatorFastScroll/blob/master/sample/src/main/res/layout/sample_styled.xml#L25-L47).

### Filtering out indicators
The fast scroller can be set up to filter out certain indicators from being shown. `showIndicator` is a predicate that you can supply (either during `setupWithRecyclerView()` or at a later time) that lets you determine whether or not each indicator should be shown. For example, this can be used to hide some indicators if the screen is too short to fit them all. [Here's a sample](https://github.com/reddit/IndicatorFastScroll/blob/master/sample/src/main/java/com/reddit/indicatorfastscroll/sample/examples/FilteredFragment.kt#L58-L61).

### Custom scroll handling

By default, the fast scroller handles scrolling the RecyclerView to the right section when it's pressed. However, in case you want to override this behavior, you can set `useDefaultScroller` to false and set up a callback with the fast scroller to handle it yourself.
##### Kotlin:
```kotlin
fastScrollerView.useDefaultScroller = false
fastScrollerView.itemIndicatorSelectedCallbacks += object : FastScrollerView.ItemIndicatorSelectedCallback {
  override fun onItemIndicatorSelected(
      indicator: FastScrollItemIndicator,
      indicatorCenterY: Int,
      itemPosition: Int
  ) {
    // Handle scrolling
  }
}
```
##### Java:
```java
fastScrollerView.setUseDefaultScroller(false);
fastScrollerView.getItemIndicatorSelectedCallbacks().add(
    new FastScrollerView.ItemIndicatorSelectedCallback() {
        @Override
        public void onItemIndicatorSelected(
            FastScrollItemIndicator indicator,
            int indicatorCenterY,
            int itemPosition
        ) {
            // Handle scrolling
        }
    }
);
```

[Here's a sample](https://github.com/reddit/IndicatorFastScroll/blob/master/sample/src/main/java/com/reddit/indicatorfastscroll/sample/examples/CustomScrollFragment.kt#L65-L76).
