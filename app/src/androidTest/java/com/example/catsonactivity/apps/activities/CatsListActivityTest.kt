package com.example.catsonactivity.apps.activities

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.catsonactivity.R
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
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import io.mockk.every
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests for app with navigation based on activities.
 *
 * This class contains tests for [CatsListActivity]. The activity is
 * launched separately by using [ActivityScenario].
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@UninstallModules(RepositoriesModule::class)
@MediumTest
class CatsListActivityTest : BaseTest() {

    private lateinit var scenario: ActivityScenario<CatsListActivity>

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

    @Before
    override fun setUp() {
        super.setUp()
        every { catsRepository.getCats() } returns catsFlow
        Intents.init()
        scenario = ActivityScenario.launch(CatsListActivity::class.java)
    }

    @After
    fun tearDown() {
        Intents.release()
        scenario.close()
    }

    @Test
    fun catsAndHeadersAreDisplayedInList() {
        // act

        onView(withId(R.id.catsRecyclerView))
            .perform(scrollToPosition(0))
            .check(matches(atPosition(0, withText("Cats: 1 … 2"))))

        // assert
        onView(withId(R.id.catsRecyclerView))
            .perform(scrollToPosition(1))
            .check(matches(atPosition(1, allOf(
                hasDescendant(allOf(withId(R.id.catNameTextView), withText("Lucky"))),
                hasDescendant(allOf(withId(R.id.catDescriptionTextView), withText("The first cat"))),
                hasDescendant(allOf(withId(R.id.favoriteImageView), withDrawable(R.drawable.ic_favorite_not, R.color.action))),
                hasDescendant(allOf(withId(R.id.deleteImageView), withDrawable(R.drawable.ic_delete, R.color.action))),
                hasDescendant(allOf(withId(R.id.catImageView), withDrawable(FakeImageLoader.createDrawable("cat1.jpg"))))
            ))))

        onView(withId(R.id.catsRecyclerView))
            .perform(scrollToPosition(2))
            .check(matches(atPosition(2, allOf(
                hasDescendant(allOf(withId(R.id.catNameTextView), withText("Tiger"))),
                hasDescendant(allOf(withId(R.id.catDescriptionTextView), withText("The second cat"))),
                hasDescendant(allOf(withId(R.id.favoriteImageView), withDrawable(R.drawable.ic_favorite, R.color.highlighted_action))),
                hasDescendant(allOf(withId(R.id.deleteImageView), withDrawable(R.drawable.ic_delete, R.color.action))),
                hasDescendant(allOf(withId(R.id.catImageView), withDrawable(FakeImageLoader.createDrawable("cat2.jpg"))))
            ))))

        onView(withId(R.id.catsRecyclerView))
            .check(matches(withItemsCount(3))) // 1 header + 2 cats
    }

    @Test
    fun clickOnCatLaunchesDetails() {
        // arrange
        every { catsRepository.getCatById(any()) } returns flowOf(cat1)

        // act
        onView(withId(R.id.catsRecyclerView))
            .perform(actionOnItemAtPosition(1, click()))

        // assert
        Intents.intended(IntentMatchers.hasExtra(CatDetailsActivity.EXTRA_CAT_ID, 1L))
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
            .check(matches(atPosition(0, withText("Cats: 1 … 1"))))
        onView(withId(R.id.catsRecyclerView))
            .perform(scrollToPosition(1))
            .check(matches(atPosition(1, allOf(
                hasDescendant(allOf(withId(R.id.catNameTextView), withText("Tiger"))),
                hasDescendant(allOf(withId(R.id.catDescriptionTextView), withText("The second cat"))),
                hasDescendant(allOf(withId(R.id.favoriteImageView), withDrawable(R.drawable.ic_favorite, R.color.highlighted_action))),
                hasDescendant(allOf(withId(R.id.deleteImageView), withDrawable(R.drawable.ic_delete, R.color.action))),
                hasDescendant(allOf(withId(R.id.catImageView), withDrawable(FakeImageLoader.createDrawable(cat2.photoUrl))))
            ))))
        onView(withId(R.id.catsRecyclerView))
            .check(matches(withItemsCount(2))) // 1 header + 1 cat
    }

    private fun assertFavorite(expectedDrawableRes: Int, expectedTintColorRes: Int? = null) {
        onView(withId(R.id.catsRecyclerView))
            .perform(scrollToPosition(1))
            .check(matches(atPosition(1,
                hasDescendant(
                    allOf(
                        withId(R.id.favoriteImageView),
                        withDrawable(expectedDrawableRes, expectedTintColorRes))
                )
            )
            ))
    }


}