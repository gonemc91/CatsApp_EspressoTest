package com.example.catsonactivity.apps.activities

import androidx.core.os.bundleOf
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.catsonactivity.R
import com.example.catsonactivity.model.Cat
import com.example.catsonactivity.testutils.BaseTest
import com.example.catsonactivity.testutils.espresso.withDrawable
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Test for app with navigation based on activities.
 *
 * This class contains tests fo [CatDetailsActivity]. The activity is
 * launched separately by using [ActivitySecenario].
 */

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
/*@UnistalModules(RepositoriesModule::class)*/
@MediumTest
class CatDetailActivityTest : BaseTest(){

    private lateinit var scenario: ActivityScenario<CatDetailsActivity>

    private val cat = Cat(
        id = 1,
        name = "Lucky",
        photoUrl = "cat.jpg",
        description = "Meow-meow",
        isFavorite = true
    )

    private val catFlow = MutableStateFlow(cat)

    @Before
    override fun setUp() {
        super.setUp()
        every{catsRepository.getCatById(any())} returns catFlow
        Intents.init()
        scenario = ActivityScenario.launch(
            CatDetailsActivity::class.java,
            bundleOf(
                CatDetailsActivity.EXTRA_CAT_ID to 1L
            )
        )
    }

    @After
    fun tearDown(){
        Intents.release()
        scenario.close()
    }

    @Test
    fun catIsDisplayed() {
        //assert

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
        onView(withId(R.id.favoriteImageView)).perform(click())
        //assert 1
        onView(withId(R.id.favoriteImageView))
            .check(matches(withDrawable(R.drawable.ic_favorite_not, R.color.action)))

    }


}