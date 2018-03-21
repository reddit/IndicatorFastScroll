package com.reddit.indicatorfastscroll.sample

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import android.view.ViewGroup
import android.widget.Button
import androidx.view.isGone
import com.reddit.indicatorfastscroll.sample.examples.*

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
    val buttonsView: ViewGroup = findViewById(R.id.main_buttons)

    Samples.values().forEach { sample ->
      val button = layoutInflater.inflate(R.layout.sample_menu_button, rootView, false).apply {
        this as Button
        text = sample.title
        setOnClickListener {
          supportFragmentManager
              .beginTransaction()
              .add(
                  R.id.main_root,
                  Fragment.instantiate(this@SampleActivity, sample.fragmentClass.name)
              )
              .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
              .addToBackStack(null)
              .commit()
        }
      }
      buttonsView.addView(button)
    }

    supportFragmentManager.addOnBackStackChangedListener {
      menuView.isGone = (supportFragmentManager.backStackEntryCount > 0)
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
