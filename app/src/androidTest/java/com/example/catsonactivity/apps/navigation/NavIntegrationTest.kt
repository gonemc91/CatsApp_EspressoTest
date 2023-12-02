package com.example.catsonactivity.apps.navigation

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.catsonactivity.R
import com.example.catsonactivity.apps.navcomponent.NavComponentActivity
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
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Test for app navigation based on Nav Component.
 *
 *
 * This class contains integration tests for Nav Components. That's why the
 * navigation is not mocked and real main activity is used
 * to start integration tests ([NavComponentActivity])
 */

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@UninstallModules(RepositoriesModule::class)
@LargeTest

class NavIntegrationTest: BaseTest() {

    private val  cat = Cat(
        id = 1,
        name = "Lucky",
        photoUrl = "cat1.jpg",
        description = "The first cat",
        isFavorite = false
    )

    private val catsFlow = MutableStateFlow(listOf(cat))

    private lateinit var scenario: ActivityScenario<NavComponentActivity>

    @Before
    override fun setUp() {
        super.setUp()
        every { catsRepository.getCats() } returns catsFlow
        every { catsRepository.getCatById(any()) } returns catsFlow.map { it.first() }
        every { catsRepository.toggleIsFavorite(any()) } answers {
            catsFlow.value = catsFlow.value.map { it.copy(isFavorite = !it.isFavorite)}
        }
        scenario = ActivityScenario.launch(NavComponentActivity::class.java)
    }

    @After
    fun ternDown(){
        scenario.close()
    }

    @Test
    fun testFavouriteFlag(){
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

    @Test
    fun testNavigateUpButton(){
        clickOnCat()
        clickOnNavigateUp()
        assertCatsListTitle()
    }

    @Test

    fun testHardwareBackButton(){
        clickOnCat()
        Espresso.pressBack()
        assertCatsListTitle()
    }

    private fun clickOnCat(){
        Espresso.onView(ViewMatchers.withId(R.id.catsRecyclerView))
            .perform(actionOnItemAtPosition(1, ViewActions.click()))
    }
    private fun clickOnToggleFavoriteInListScreen(){
        Espresso.onView(ViewMatchers.withId(R.id.catsRecyclerView))
            .perform(actionOnItemAtPosition(1, clickOnView(R.id.favoriteImageView)))
    }

    private fun clickOnToggleFavoriteInDetails(){
        Espresso.onView(Matchers.allOf(ViewMatchers.withId(R.id.favoriteImageView),
            Matchers.not(ViewMatchers.isDescendantOfA(ViewMatchers.withId(R.id.catsRecyclerView)))
        ))
            .perform(ViewActions.click())
    }

    private fun clickOnGoBack(){
        Espresso.onView(ViewMatchers.withId(R.id.goBackButton)).perform(ViewActions.click())
    }

    private fun assertIsFavoriteFlagActiveInListScreen(){
        Espresso.onView(ViewMatchers.withId(R.id.catsRecyclerView))
            .perform(scrollToPosition(1))
            .check(ViewAssertions.matches(atPosition(1, ViewMatchers.hasDescendant(
                Matchers.allOf(
                    ViewMatchers.withId(R.id.favoriteImageView),
                    withDrawable(R.drawable.ic_favorite, R.color.highlighted_action)
                )
            ))))
    }
    private fun assertIsFavoriteFlagInactiveInListScreen() {
        Espresso.onView(withId(R.id.catsRecyclerView))
            .perform(scrollToPosition(1))
            .check(ViewAssertions.matches(atPosition(1, hasDescendant(
                Matchers.allOf(
                    withId(R.id.favoriteImageView),
                    withDrawable(R.drawable.ic_favorite_not, R.color.action)
                )
            ))))
    }


    private fun assertIsFavoriteFlagActiveInDetailsScreen(){
        Espresso.onView(Matchers.allOf(ViewMatchers.withId(R.id.favoriteImageView), Matchers.not(ViewMatchers.isDescendantOfA(ViewMatchers.withId(R.id.catsRecyclerView)))))
            .check(ViewAssertions.matches(
                withDrawable(R.drawable.ic_favorite, R.color.highlighted_action)))
    }



    private fun assertCatsListTitle(){
        val context = ApplicationProvider.getApplicationContext<Context>()
        scenario.onActivity { activity->
            Assert.assertEquals(
            context.getString(R.string.fragment_cats_title),
            activity.supportActionBar?.title?.toString()
            )
        }
    }

    private fun assertCatDetailsTitle(){
        val context = ApplicationProvider.getApplicationContext<Context>()
        scenario.onActivity { activity ->
            Assert.assertEquals(
                context.getString(R.string.fragment_cat_details),
                activity.supportActionBar?.title?.toString()
            )
        }
    }

    private fun clickOnNavigateUp(){
        Espresso.onView(ViewMatchers.withContentDescription(
            androidx.appcompat.R.string.abc_action_bar_up_description
        )).perform(ViewActions.click())
    }

}