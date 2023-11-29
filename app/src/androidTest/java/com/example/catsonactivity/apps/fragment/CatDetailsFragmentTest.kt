package com.example.catsonactivity.apps.fragment

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.catsonactivity.apps.fragments.FragmentRouter
import com.example.catsonactivity.apps.fragments.di.FragmentRouterModule
import com.example.catsonactivity.di.RepositoriesModule
import com.example.catsonactivity.model.Cat
import com.example.catsonactivity.testutils.BaseTest
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.runner.RunWith
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Test for app with navigation based on fragment.
 *
 * This class contains tests for [CatDetailsFragment]. Real navigation
 * is replaced by a fake [FragmentRouter] created by Mockk.
 *
 * The fragment itself is launched in a separate empty activity container by
 * using [launchHiltFragment] method.
 */

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@UninstallModules(RepositoriesModule::class, FragmentRouterModule::class)
@MediumTest

class CatDetailsFragmentTest: BaseTest() {

    @Inject
    lateinit var fragmentRouter: FragmentRouter

    private val cat = Cat(
        id = 1,
        name = "Lucky",
        photoUrl = "cat.jpg",
        description = "Meow-meow",
        isFavorite = true
    )

    private val catsFlow = MutableStateFlow(cat)

    private lateinit var scenario: AutoCloseable

    @Before
    override fun setUp() {
        super.setUp()
        every { catsRepository.getCatById(any()) } returns catsFlow

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