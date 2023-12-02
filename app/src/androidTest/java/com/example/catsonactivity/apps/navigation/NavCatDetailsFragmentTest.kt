package com.example.catsonactivity.apps.navigation

import androidx.navigation.NavController
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.catsonactivity.R
import com.example.catsonactivity.apps.navcomponent.NavCatDetailsFragment
import com.example.catsonactivity.apps.navcomponent.NavCatDetailsFragmentArgs
import com.example.catsonactivity.apps.navcomponent.NavCatsListFragment
import com.example.catsonactivity.di.RepositoriesModule
import com.example.catsonactivity.model.Cat
import com.example.catsonactivity.testutils.BaseTest
import com.example.catsonactivity.testutils.FakeImageLoader
import com.example.catsonactivity.testutils.espresso.withDrawable
import com.example.catsonactivity.testutils.launchNavHiltFragment
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests for app with navigation based on Nav Component.
 *
 * This class contain tests fo [NavCatsListFragment]. Real navigation
 * is replaced by a fake [NavController] created by MockK.
 * The fragment itself is launched in a separate empty activity container by
 * using [launchNavHiltFragment] method.
 */

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@UninstallModules(RepositoriesModule::class)
@MediumTest


class NavCatDetailsFragmentTest: BaseTest() {

    @RelaxedMockK
    lateinit var navController: NavController

    private val cat = Cat(
        id = 1,
        name = "Lucky",
        photoUrl = "cat.jpg",
        description = "Meow-meow",
        isFavorite = true
    )

    private val catFlow = MutableStateFlow(cat)

    private lateinit var scenario: AutoCloseable

    @Before
    override fun setUp() {
        super.setUp()
        every { catsRepository.getCatById(any()) } returns catFlow
        val args = NavCatDetailsFragmentArgs(catId = 1L)
        scenario = launchNavHiltFragment<NavCatDetailsFragment>(navController, args.toBundle())
    }

    @After
    fun tearDown(){
        scenario.close()
    }

    @Test
    fun catIsDisplayed() {
        //assert
        Espresso.onView(ViewMatchers.withId(R.id.catNameTextView))
            .check(ViewAssertions.matches(ViewMatchers.withText("Lucky")))
        Espresso.onView(ViewMatchers.withId(R.id.catDescriptionTextView))
            .check(ViewAssertions.matches(ViewMatchers.withText("Meow-meow")))
        Espresso.onView(ViewMatchers.withId(R.id.favoriteImageView))
            .check(
                ViewAssertions.matches(
                    withDrawable(
                        R.drawable.ic_favorite,
                        R.color.highlighted_action
                    )
                )
            )
        Espresso.onView(ViewMatchers.withId(R.id.catImageView))
            .check(ViewAssertions.matches(withDrawable(FakeImageLoader.createDrawable(cat.photoUrl))))
    }


    @Test
    fun toggleFavoriteTogglesFlag(){
        //arranged
        every { catsRepository.toggleIsFavorite(any()) } answers {
            val cat = firstArg<Cat>()
            val newCat = cat.copy(isFavorite = !cat.isFavorite)
            catFlow.value = newCat
        }

        //act 1 - turn off favorite flag
        Espresso.onView(ViewMatchers.withId(R.id.favoriteImageView)).perform(ViewActions.click())
        //assert 1
        Espresso.onView(ViewMatchers.withId(R.id.favoriteImageView))
            .check(ViewAssertions.matches(withDrawable(R.drawable.ic_favorite_not, R.color.action)))

        //act 2 - turn off favorite flag
        Espresso.onView(ViewMatchers.withId(R.id.favoriteImageView)).perform(ViewActions.click())
        //assert 2
        Espresso.onView(ViewMatchers.withId(R.id.favoriteImageView))
            .check(
                ViewAssertions.matches(
                    withDrawable(
                        R.drawable.ic_favorite,
                        R.color.highlighted_action
                    )
                )
            )
    }

    @Test
    fun clickOnBackFinishedActivity(){
        Espresso.onView(ViewMatchers.withId(R.id.goBackButton)).perform(ViewActions.click())
        verify(exactly = 1) { navController.popBackStack() }
    }

}