package com.example.catsonactivity.apps.activities

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.catsonactivity.R
import com.example.catsonactivity.di.RepositoriesModule
import com.example.catsonactivity.model.Cat
import com.example.catsonactivity.testutils.BaseTest
import com.example.catsonactivity.testutils.espresso.actionOnItemAtPosition
import com.example.catsonactivity.testutils.espresso.atPosition
import com.example.catsonactivity.testutils.espresso.clickOnView
import com.example.catsonactivity.testutils.espresso.scrollToPosition
import com.example.catsonactivity.testutils.espresso.withDrawable
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import io.mockk.every
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Tests for app with navigation based on activities.
 *
 * This class contains integration tests so it launches the first
 * main activity of the application - [CatsListActivity] and then
 * it tests the navigation and data consistency between screens.
 */


@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@UninstallModules(RepositoriesModule::class)
@LargeTest

class CatsActivitiesIntegrationTest: BaseTest() {

    private lateinit var scenario: ActivityScenario<CatsListActivity>

    private val cat = Cat(
        id = 1,
        name = "Lucky",
        photoUrl = "cat.jpg",
        description = "The first cat-",
        isFavorite = false
    )

    private val catsFlow = MutableStateFlow(listOf(cat))

    @Before
    override fun setUp() {
        super.setUp()
        every { catsRepository.getCats() } returns  catsFlow
        every { catsRepository.getCatById(any()) } returns  catsFlow.map { it.first() }
        every { catsRepository.toggleIsFavorite(any()) } answers {
            catsFlow.value = catsFlow.value.map { it.copy(isFavorite = !it.isFavorite) }
        }
        Intents.init()
        scenario = ActivityScenario.launch(CatsListActivity::class.java)
    }

    @After
    fun tearDown(){
        Intents.release()
        scenario.close()
    }

    @Test
    fun testFavoriteFlag(){
        clickOnToggleFavoriteInListScreen()
        clickOnCat()
        assertIsFavoriteFlagActiveInDetailsScreen()
        clickOnGoBack()
        assertIsFavoriteFlagActiveInListScreen()
        clickOnCat()
        clickOnToggleFavoriteInDetails()
        clickOnGoBack()
        assertIsFavoriteFlagInactiveInListScreen()
    }

    private fun clickOnToggleFavoriteInListScreen(){
        onView(withId(R.id.catsRecyclerView))
            .perform(actionOnItemAtPosition(1, clickOnView(R.id.favoriteImageView)))
    }
    private fun clickOnCat(){
        onView(withId(R.id.catsRecyclerView))
            .perform(actionOnItemAtPosition(1, click()))
    }

    private fun assertIsFavoriteFlagActiveInDetailsScreen(){
        onView(Matchers.allOf(withId(R.id.favoriteImageView), Matchers.not(isDescendantOfA(withId(R.id.catsRecyclerView)))))
            .check(ViewAssertions.matches(withDrawable(R.drawable.ic_favorite, R.color.highlighted_action)))
    }

    private fun clickOnGoBack(){
        onView(withId(R.id.goBackButton)).perform(click())
    }
    private fun assertIsFavoriteFlagActiveInListScreen(){
        onView(withId(R.id.catsRecyclerView))
            .perform(scrollToPosition(1))
            .check(ViewAssertions.matches(atPosition(1, hasDescendant(
                Matchers.allOf(
                    withId(R.id.favoriteImageView),
                    withDrawable(R.drawable.ic_favorite, R.color.highlighted_action))
            )
            )))
    }

    private fun clickOnToggleFavoriteInDetails(){
        onView(Matchers.allOf(withId(R.id.favoriteImageView), Matchers.not(isDescendantOfA(withId(R.id.catsRecyclerView)))))
            .perform(click())
    }

    private fun assertIsFavoriteFlagInactiveInListScreen(){
        onView(withId(R.id.catsRecyclerView))
            .perform(scrollToPosition(1))
            .check(ViewAssertions.matches(atPosition(1, hasDescendant(
                Matchers.allOf(
                    withId(R.id.favoriteImageView),
                    withDrawable(R.drawable.ic_favorite_not, R.color.action)
                )
            ))))
    }
}