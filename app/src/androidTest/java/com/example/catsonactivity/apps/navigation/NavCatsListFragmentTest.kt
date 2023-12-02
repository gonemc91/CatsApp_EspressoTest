package com.example.catsonactivity.apps.navigation

import androidx.navigation.NavController
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.catsonactivity.R
import com.example.catsonactivity.apps.navcomponent.NavCatsListFragment
import com.example.catsonactivity.apps.navcomponent.NavCatsListFragmentDirections
import com.example.catsonactivity.di.RepositoriesModule
import com.example.catsonactivity.model.Cat
import com.example.catsonactivity.testutils.BaseTest
import com.example.catsonactivity.testutils.FakeImageLoader
import com.example.catsonactivity.testutils.espresso.actionOnItemAtPosition
import com.example.catsonactivity.testutils.espresso.atPosition
import com.example.catsonactivity.testutils.espresso.clickOnView
import com.example.catsonactivity.testutils.espresso.scrollToPosition
import com.example.catsonactivity.testutils.espresso.withDrawable
import com.example.catsonactivity.testutils.espresso.withItemsCount
import com.example.catsonactivity.testutils.launchNavHiltFragment
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.hamcrest.Matchers.allOf
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

class NavCatsListFragmentTest: BaseTest() {

    @RelaxedMockK
    lateinit var navController: NavController

    private val cat1 = Cat(
        id = 1,
        name = "Lucky",
        photoUrl = "cat1.jpg",
        description = "The first cat",
        isFavorite = false
    )
    private val cat2 = Cat(
        id = 2,
        name = "Tiger",
        photoUrl = "cat2.jpg",
        description = "The second cat",
        isFavorite = true
    )

    private val catsFlow = MutableStateFlow(listOf(cat1, cat2))

    private lateinit var scenario: AutoCloseable

    @Before
    override fun setUp() {
        super.setUp()
        every { catsRepository.getCats() } returns catsFlow
        scenario = launchNavHiltFragment<NavCatsListFragment>(navController)
    }

    @After
    fun tearDown(){
        scenario.close()
    }

    @Test
    fun catsAndHeaderAreDisplayedInList(){
        //act
        onView(withId(R.id.catsRecyclerView))
            .perform(scrollToPosition(0))
            .check(ViewAssertions.matches(atPosition(0, withText("Cats: 1 … 2"))))


        //assert
        onView(withId(R.id.catsRecyclerView))
            .perform(scrollToPosition(1))
            .check(ViewAssertions.matches(atPosition(1, allOf(
                hasDescendant(allOf(withId(R.id.catNameTextView), withText("Lucky"))),
                hasDescendant(allOf(withId(R.id.catDescriptionTextView), withText("The first cat"))),
                hasDescendant(allOf(withId(R.id.favoriteImageView), withDrawable(R.drawable.ic_favorite_not, R.color.action))),
                hasDescendant(allOf(withId(R.id.deleteImageView), withDrawable(R.drawable.ic_delete, R.color.action))),
                hasDescendant(allOf(withId(R.id.catImageView), withDrawable(FakeImageLoader.createDrawable("cat1.jpg"))))
            )
            )))

        onView(withId(R.id.catsRecyclerView))
            .perform(scrollToPosition(2))
            .check(ViewAssertions.matches(atPosition(2, allOf(
                hasDescendant(allOf(withId(R.id.catNameTextView), withText("Tiger"))),
                hasDescendant(allOf(withId(R.id.catDescriptionTextView), withText("The second cat"))),
                hasDescendant(allOf(withId(R.id.favoriteImageView), withDrawable(R.drawable.ic_favorite, R.color.highlighted_action))),
                hasDescendant(allOf(withId(R.id.deleteImageView), withDrawable(R.drawable.ic_delete, R.color.action))),
                hasDescendant(allOf(withId(R.id.catImageView), withDrawable(FakeImageLoader.createDrawable("cat2.jpg"))))
            )
            )))

        onView(withId(R.id.catsRecyclerView))
            .check(ViewAssertions.matches(withItemsCount(3))) // 1 header + 2 cats

    }

    @Test
    fun clickOnCatLaunchesDetails(){
        //act
        onView(withId(R.id.catsRecyclerView))
            .perform(actionOnItemAtPosition(1, ViewActions.click()))

        //assert
        val expectedDirection = NavCatsListFragmentDirections
            .actionNavCatsListFragmentToNavCatDetailsFragment(1L)
        verify {
            navController.navigate(expectedDirection)
        }
    }

    @Test
    fun clickOnFavoriteTogglesFlag() {
        // arrange
        every { catsRepository.toggleIsFavorite(any()) } answers {
            val cat = firstArg<Cat>()
            catsFlow.value = listOf(
                cat.copy(isFavorite = !cat.isFavorite),
                cat2
            )
        }

        // act 1 - turn on a favorite flag
        onView(withId(R.id.catsRecyclerView))
            .perform(actionOnItemAtPosition(1, clickOnView(R.id.favoriteImageView)))

        // assert 1
        assertFavorite(R.drawable.ic_favorite, R.color.highlighted_action)

        // act 2 - turn off a favorite flag
        onView(withId(R.id.catsRecyclerView))
            .perform(actionOnItemAtPosition(1, clickOnView(R.id.favoriteImageView)))

        // assert 2
        assertFavorite(R.drawable.ic_favorite_not, R.color.action)
    }

    @Test
    fun clickOnDeleteRemovesCatFromList() {
        // arrange
        every { catsRepository.delete(any()) } answers {
            catsFlow.value = listOf(cat2)
        }

        // act
        onView(withId(R.id.catsRecyclerView))
            .perform(actionOnItemAtPosition(1, clickOnView(R.id.deleteImageView)))

        // assert
        onView(withId(R.id.catsRecyclerView))
            .perform(scrollToPosition(0))
            .check(ViewAssertions.matches(atPosition(0, withText("Cats: 1 … 1"))))
        onView(withId(R.id.catsRecyclerView))
            .perform(scrollToPosition(1))
            .check(
                ViewAssertions.matches(
                    atPosition(
                        1, allOf(
                            hasDescendant(allOf(withId(R.id.catNameTextView), withText("Tiger"))),
                            hasDescendant(
                                allOf(withId(R.id.catDescriptionTextView), withText("The second cat"))
                            ),
                            hasDescendant(
                                allOf(withId(R.id.favoriteImageView), withDrawable(R.drawable.ic_favorite, R.color.highlighted_action))
                            ),
                            hasDescendant(allOf(withId(R.id.deleteImageView), withDrawable(R.drawable.ic_delete, R.color.action))
                            ),
                            hasDescendant(
                                allOf(withId(R.id.catImageView), withDrawable(FakeImageLoader.createDrawable(cat2.photoUrl))
                                ))))))

        onView(withId(R.id.catsRecyclerView))
            .check(ViewAssertions.matches(withItemsCount(2))) // 1 header + 1 cat
    }

    private fun assertFavorite(expectedDrawableRes: Int, expectedTintColorRes: Int? = null) {
        onView(withId(R.id.catsRecyclerView))
            .perform(scrollToPosition(1))
            .check(ViewAssertions.matches(atPosition(1, hasDescendant(
                            allOf(withId(R.id.favoriteImageView), withDrawable(expectedDrawableRes, expectedTintColorRes)
                            )))))
    }




}