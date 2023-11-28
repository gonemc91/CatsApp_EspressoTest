package com.example.catsonactivity.testutils.rule

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description


/**
 * This test rules replaces a main coroutines dispatcher by the specified
 * [TestDispatcher]
 */

class TestViewModelScopeRule (
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
): TestWatcher(){
    override fun starting(description: Description) {

        super.starting(description)
        Dispatchers.setMain(testDispatcher)
    }


    override fun finished(description: Description) {
        super.finished(description)
        Dispatchers.resetMain()
    }
}