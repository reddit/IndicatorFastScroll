package com.reddit.indicatorfastscroll

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.reddit.indicatorfastscroll.test.R
import org.hamcrest.Matchers.*
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
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

}
