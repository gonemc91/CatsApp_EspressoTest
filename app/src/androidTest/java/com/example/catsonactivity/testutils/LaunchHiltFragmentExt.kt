package com.example.catsonactivity.testutils

import android.content.ComponentName
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.example.catsonactivity.testutils.fragments.TestFragmentActivity

/**
 * Launch a fragment manager by Hilt in the test activity container.
 *
 * @see TestFraggmentActivity
 *
 */

inline fun <reified T : Fragment> launchHiltFragment(
    noinline creator:  (()->T)? = null
): AutoCloseable{
    val intent = Intent.makeMainActivity(
        ComponentName(
            ApplicationProvider.getApplicationContext(),
            TestFragmentActivity::class.java
        )
    )

    return ActivityScenario.launch<TestFragmentActivity>(intent).onActivity{
        val fragment = creator?.invoke() ?: it.supportFragmentManager.fragmentFactory.instantiate(
            T::class.java.classLoader!!,
            T::class.java.name
        )
        it.supportFragmentManager
            .beginTransaction()
            .add(android.R.id.content, fragment)
            .commitNow()
    }
}