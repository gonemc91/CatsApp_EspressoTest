package com.example.catsonactivity.testutils

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.catsonactivity.model.CatsRepository
import com.example.catsonactivity.testutils.rule.FakeImageLoaderRule
import com.example.catsonactivity.testutils.rule.TestViewModelScopeRule
import dagger.hilt.android.testing.HiltAndroidRule
import io.mockk.junit4.MockKRule
import org.junit.Before
import org.junit.Rule
import javax.inject.Inject


/**
 * Base class for all UI tests.
 */
open class BaseTest {


     //Substitutes MainDispatcher on [UnconfinedTestDispatcher] for coroutine.
    @get:Rule
    val testViewModelScopeRule = TestViewModelScopeRule()

    //Replaced the method notification live-dates. Send data without change thread.
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    //Substitutes on mock all dependencies with annotation @Mockk.
    @get:Rule
    val mockkRule = MockKRule(this)

    //Substitute hilt module dependencies on test module.Request "dependencies module" uses  @Inject.
    @get: Rule
    val hiltRule = HiltAndroidRule(this)

    //Loading image without going in network.
    @get: Rule
    val fakeImageLoaderRule = FakeImageLoaderRule()

    @Inject
    lateinit var catsRepository: CatsRepository

    @Before
    open fun setUp(){
        hiltRule.inject()
    }

}