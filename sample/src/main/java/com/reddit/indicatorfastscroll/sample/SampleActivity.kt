package com.reddit.indicatorfastscroll.sample

import android.os.AsyncTask
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import com.reddit.indicatorfastscroll.sample.examples.CustomScrollFragment
import com.reddit.indicatorfastscroll.sample.examples.FilteredFragment
import com.reddit.indicatorfastscroll.sample.examples.JustTextFragment
import com.reddit.indicatorfastscroll.sample.examples.StyledFragment
import com.reddit.indicatorfastscroll.sample.examples.TextWithIconFragment
import com.thedeanda.lorem.LoremIpsum

/**
 * The meaty part is in the sample fragments.
 *
 * @see JustTextFragment
 * @see TextWithIconFragment
 */
class SampleActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.main)
    val rootView: ViewGroup = findViewById(R.id.main_root)
    val menuView: ViewGroup = findViewById(R.id.main_menu)
    val sampleToolbarView: Toolbar = findViewById(R.id.main_sample_toolbar)
    val sampleButtonsView: ViewGroup = findViewById(R.id.main_sample_buttons)

    sampleToolbarView.setNavigationOnClickListener {
      supportFragmentManager.popBackStack()
    }

    Samples.values().forEach { sample ->
      val button = layoutInflater.inflate(R.layout.sample_menu_button, rootView, false).apply {
        this as Button
        text = sample.title
        setOnClickListener {
          supportFragmentManager
              .beginTransaction()
              .replace(
                  R.id.main_sample_fragment,
                  Fragment.instantiate(this@SampleActivity, sample.fragmentClass.name)
              )
              .addToBackStack(null)
              .commit()
        }
      }
      sampleButtonsView.addView(button)
    }

    fun updateViews() {
      val sampleFragment = supportFragmentManager.findFragmentById(R.id.main_sample_fragment)
      val isShowingSample = (sampleFragment != null)
      menuView.isGone = isShowingSample
      sampleToolbarView.isGone = !isShowingSample
      sampleToolbarView.title = sampleFragment?.let {
        Samples.values().find { it.fragmentClass == sampleFragment::class.java }?.title
      }
    }

    updateViews()
    supportFragmentManager.addOnBackStackChangedListener {
      updateViews()
    }

    AsyncTask.execute {
      // Preload the sample data. Not thread-safe, but not a big deal.
      LoremIpsum.getInstance()
    }
  }

  enum class Samples(val title: String, val fragmentClass: Class<out Fragment>) {
    JustText("Just text", JustTextFragment::class.java),
    TextWithIcon("Text with icon", TextWithIconFragment::class.java),
    Styled("Styled", StyledFragment::class.java),
    Filtered("Filtered", FilteredFragment::class.java),
    CustomScroll("Custom scroll", CustomScrollFragment::class.java)
  }

}
