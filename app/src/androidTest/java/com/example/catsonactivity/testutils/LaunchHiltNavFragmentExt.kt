package com.example.catsonactivity.testutils

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.example.catsonactivity.testutils.fragments.TestFragmentActivity


/**
 * Launch a fragment manager by Hilt and Navigation Component in the test
 * activity container.
 *
 * @see TestFragmentActivity
 */

inline fun <reified T: Fragment>launchNavHiltFragment(
    navController: NavController,
    arg: Bundle? = null
): AutoCloseable{
    val intent = Intent.makeMainActivity(
        ComponentName(
            ApplicationProvider.getApplicationContext(),
            TestFragmentActivity::class.java
        )
    )

    return ActivityScenario.launch<TestFragmentActivity>(intent).onActivity {
        val fragment = it.supportFragmentManager.fragmentFactory.instantiate(
            T::class.java.classLoader!!,
            T::class.java.name
        )
        fragment.arguments = arg

        it.supportFragmentManager
            .beginTransaction()
            .add(android.R.id.content, fragment)
            .commitNow()

        Navigation.setViewNavController(
            it.findViewById(android.R.id.content),
            navController
        )
    }
}