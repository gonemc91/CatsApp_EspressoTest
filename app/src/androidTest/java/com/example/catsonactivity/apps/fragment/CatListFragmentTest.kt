package com.example.catsonactivity.apps.fragment

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.catsonactivity.R
import com.example.catsonactivity.apps.fragments.CatsListFragment
import com.example.catsonactivity.apps.fragments.FragmentRouter
import com.example.catsonactivity.apps.fragments.di.FragmentRouterModule
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
import com.example.catsonactivity.testutils.launchHiltFragment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Test for app with navigation based on fragments
 *
 * This class contains test for [CatsListFragment]. Real navigation
 * is replaced by a fake [FragmentRouter] created by MockK.
 * The fragment itself is launched in a separate emty activity container by
 * using [launchHiltFragment] method.
 */

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@UninstallModules(RepositoriesModule::class, FragmentRouterModule:: class)
@MediumTest

class CatListFragmentTest: BaseTest() {

    @Inject
    lateinit var router: FragmentRouter

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
        description = "The second cat-",
        isFavorite = true
    )

    private val catsFlow = MutableStateFlow(listOf(cat1, cat2))

    private lateinit var scenario: AutoCloseable

    @Before
    override fun setUp() {
        super.setUp()
        every { catsRepository.getCats() } returns catsFlow
        scenario = launchHiltFragment<CatsListFragment>()
    }

    @After
    fun tearDown(){
        scenario.close()
    }
    @Test
    fun catsAndHeadersAreDisplayedInList() {
        // act
        Espresso.onView(ViewMatchers.withId(R.id.catsRecyclerView))
            .perform(scrollToPosition(0))
            .check(ViewAssertions.matches(atPosition(0, ViewMatchers.withText("Cats: 1 … 2"))))

        // assert
        Espresso.onView(ViewMatchers.withId(R.id.catsRecyclerView))
            .perform(scrollToPosition(1))
            .check(ViewAssertions.matches(atPosition(1, Matchers.allOf(
                ViewMatchers.hasDescendant(Matchers.allOf(ViewMatchers.withId(R.id.catNameTextView), ViewMatchers.withText("Lucky"))),
                ViewMatchers.hasDescendant(Matchers.allOf(ViewMatchers.withId(R.id.catDescriptionTextView), ViewMatchers.withText("The first cat"))),
                ViewMatchers.hasDescendant(Matchers.allOf(ViewMatchers.withId(R.id.favoriteImageView), withDrawable(R.drawable.ic_favorite_not, R.color.action))),
                ViewMatchers.hasDescendant(Matchers.allOf(ViewMatchers.withId(R.id.deleteImageView), withDrawable(R.drawable.ic_delete, R.color.action))),
                ViewMatchers.hasDescendant(Matchers.allOf(ViewMatchers.withId(R.id.catImageView), withDrawable(FakeImageLoader.createDrawable("cat1.jpg")))
                ))))
            )

        Espresso.onView(ViewMatchers.withId(R.id.catsRecyclerView))
            .perform(scrollToPosition(2))
            .check(ViewAssertions.matches(atPosition(2, Matchers.allOf(
                ViewMatchers.hasDescendant(Matchers.allOf(ViewMatchers.withId(R.id.catNameTextView), ViewMatchers.withText("Tiger"))),
                ViewMatchers.hasDescendant(Matchers.allOf(ViewMatchers.withId(R.id.catDescriptionTextView), ViewMatchers.withText("The second cat"))),
                ViewMatchers.hasDescendant(Matchers.allOf(ViewMatchers.withId(R.id.favoriteImageView), withDrawable(R.drawable.ic_favorite, R.color.highlighted_action))),
                ViewMatchers.hasDescendant(Matchers.allOf(ViewMatchers.withId(R.id.deleteImageView), withDrawable(R.drawable.ic_delete, R.color.action))),
                ViewMatchers.hasDescendant(Matchers.allOf(ViewMatchers.withId(R.id.catImageView), withDrawable(FakeImageLoader.createDrawable("cat2.jpg")))
                ))))
            )

        Espresso.onView(ViewMatchers.withId(R.id.catsRecyclerView))
            .check(ViewAssertions.matches(withItemsCount(3))) // 1 header + 2 cats
    }

    @Test
    fun clickOnCatLaunchesDetails() {
        // act
        Espresso.onView(ViewMatchers.withId(R.id.catsRecyclerView))
            .perform(actionOnItemAtPosition(1, ViewActions.click()))

        // assert
        verify {
            router.showDetails(1L)
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
        Espresso.onView(ViewMatchers.withId(R.id.catsRecyclerView))
            .perform(actionOnItemAtPosition(1, clickOnView(R.id.favoriteImageView)))

        // assert 1
        assertFavorite(R.drawable.ic_favorite, R.color.highlighted_action)

        // act 2 - turn off a favorite flag
        Espresso.onView(ViewMatchers.withId(R.id.catsRecyclerView))
            .perform(actionOnItemAtPosition(1, clickOnView(R.id.favoriteImageView)))

        // assert 2
        assertFavorite(R.drawable.ic_favorite_not, R.color.action)
    }
    private fun assertFavorite(expectedDrawableRes: Int, expectedTintColorRes: Int? = null) {
        Espresso.onView(ViewMatchers.withId(R.id.catsRecyclerView))
            .perform(scrollToPosition(1))
            .check(ViewAssertions.matches(atPosition(1, ViewMatchers.hasDescendant(
                            Matchers.allOf(
                                ViewMatchers.withId(R.id.favoriteImageView),
                                withDrawable(expectedDrawableRes, expectedTintColorRes)
                            )))))
    }

    @Module
    @InstallIn(SingletonComponent::class)
    class FakeFragmentRouterModule{
        @Provides
        @Singleton
        fun bindRouter(): FragmentRouter{
            return mockk(relaxed = true)
        }
    }
}