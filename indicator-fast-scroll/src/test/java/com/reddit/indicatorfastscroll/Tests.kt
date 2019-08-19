package com.reddit.indicatorfastscroll

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.reddit.indicatorfastscroll.test.R
import org.hamcrest.Matchers.*
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(AndroidJUnit4::class)
@Config(sdk = [21], qualifiers = "xhdpi")
@LooperMode(LooperMode.Mode.PAUSED)
@LargeTest
class Tests {

  @get:Rule
  val activityRule = ActivityTestRule(TestActivity::class.java)
  val activity: TestActivity get() = activityRule.activity

  @Test
  fun checkTextBatching() {
    val items = listOf(
        TestActivity.ListItem("A"),
        TestActivity.ListItem("B", android.R.drawable.ic_dialog_alert),
        TestActivity.ListItem("C"),
        TestActivity.ListItem("D"),
        TestActivity.ListItem("E", android.R.drawable.ic_dialog_dialer),
        TestActivity.ListItem("F"),
        TestActivity.ListItem("G"),
        TestActivity.ListItem("H"),
        TestActivity.ListItem("I", android.R.drawable.ic_dialog_email),
        TestActivity.ListItem("J"),
        TestActivity.ListItem("K"),
        TestActivity.ListItem("L"),
        TestActivity.ListItem("M"),
        TestActivity.ListItem("N"),
        TestActivity.ListItem("O", android.R.drawable.ic_dialog_info)
    )
    val expectedTexts = listOf(
        "A", "C\nD", "F\nG\nH", "J\nK\nL\nM\nN"
    )
    val expectedIconCount = items.count { it.iconRes != null }

    activity.runOnUiThread {
      activity.presentData(items)
    }
    onView(withId(R.id.test_fastscroller))
        .check(matches(
            allOf(expectedTexts.map { withChild(withText(it)) })
        ))
        .check(matches(
            hasChildCount(expectedTexts.size + expectedIconCount)
        ))
  }

  @Test
  fun checkNullIndicatorDoesntShow() {
    val items = listOf(
        TestActivity.ListItem("A"),
        TestActivity.ListItem("B", showInFastScroll = false),
        TestActivity.ListItem("C")
    )
    val expectedText = "A\nC"

    activity.runOnUiThread {
      activity.presentData(items)
    }
    onView(withId(R.id.test_fastscroller)).check(matches(withChild(withText(expectedText))))
  }

  @Test
  fun checkScrollsWhenTapped() {
    val items = listOf(
        TestActivity.ListItem("A"),
        TestActivity.ListItem("B", android.R.drawable.ic_dialog_alert),
        TestActivity.ListItem("C"),
        TestActivity.ListItem("D"),
        TestActivity.ListItem("E", android.R.drawable.ic_dialog_dialer),
        TestActivity.ListItem("F"),
        TestActivity.ListItem("G"),
        TestActivity.ListItem("H"),
        TestActivity.ListItem("I"),
        TestActivity.ListItem("J"),
        TestActivity.ListItem("K"),
        TestActivity.ListItem("L", android.R.drawable.ic_dialog_email),
        TestActivity.ListItem("M"),
        TestActivity.ListItem("N"),
        TestActivity.ListItem("O", android.R.drawable.ic_dialog_info),
        TestActivity.ListItem("P"),
        TestActivity.ListItem("Q", android.R.drawable.ic_dialog_map)
    )
    // For simplicity, these have unbatched text indicators
    val firstItem = items.first()
    val targetItem = items.find { it.text == "P" }!!

    activity.runOnUiThread {
      activity.presentData(items)
    }
    val targetViewMatcher = allOf(
        withText(targetItem.text),
        withParent(withId(R.id.test_recyclerview))
    )
    val topViewMatcher = allOf(
        withText(firstItem.text),
        withParent(withId(R.id.test_recyclerview))
    )
    onView(withId(R.id.test_recyclerview)).check(matches(allOf(
        not(withChild(allOf(
            targetViewMatcher,
            isCompletelyDisplayed()
        ))),
        withChild(allOf(
            topViewMatcher,
            isCompletelyDisplayed()
        ))
    )))
    onView(allOf(
        withText(targetItem.text),
        withParent(withId(R.id.test_fastscroller))
    )).perform(ViewActions.longClick())
    onView(targetViewMatcher).check(matches(isCompletelyDisplayed()))
    onView(withId(R.id.test_recyclerview)).check(matches(
        not(withChild(allOf(
            topViewMatcher,
            isCompletelyDisplayed()
        )))
    ))
  }

  @Test
  fun checkUpdateItemIndicatorsOnlyCalledOnceAfterMultiplePosts() {
    val items = listOf(
        TestActivity.ListItem("A"),
        TestActivity.ListItem("B"),
        TestActivity.ListItem("C")
    )

    val testItemIndicatorsBuilder = TestItemIndicatorsBuilder()
    activity.fastScrollerView.itemIndicatorsBuilder = testItemIndicatorsBuilder
    activity.runOnUiThread {
      // Call adapter.notifyDataSetChanged() multiple times in the same frame
      activity.presentData(items)
      activity.presentData(items)
      activity.presentData(items)
    }
    onView(withId(R.id.test_fastscroller)).check(matches(anything())) // Wait for idle
    assertEquals(1, testItemIndicatorsBuilder.timesBuildCalled)

    activity.runOnUiThread {
      activity.presentData(items)
      activity.presentData(items)
      activity.presentData(items)
    }
    onView(withId(R.id.test_fastscroller)).check(matches(anything()))
    assertEquals(2, testItemIndicatorsBuilder.timesBuildCalled)
  }

  @Test
  fun checkAdapterChange() {
    val items1 = listOf(
      TestActivity.ListItem("A"),
      TestActivity.ListItem("B"),
      TestActivity.ListItem("C")
    )
    val expectedText1 = "A\nB\nC"
    val items2 = listOf(
      TestActivity.ListItem("D"),
      TestActivity.ListItem("E"),
      TestActivity.ListItem("F")
    )
    val expectedText2 = "D\nE\nF"

    activity.runOnUiThread {
      activity.presentData(items1)
    }
    onView(withId(R.id.test_fastscroller))
      .check(matches(withChild(withText(expectedText1))))
      .check(matches(hasChildCount(1)))

    activity.runOnUiThread {
      activity.recreateAdapter()
      activity.presentData(items2)
    }
    onView(withId(R.id.test_fastscroller))
      .check(matches(withChild(withText(expectedText2))))
      .check(matches(hasChildCount(1)))
  }

}
