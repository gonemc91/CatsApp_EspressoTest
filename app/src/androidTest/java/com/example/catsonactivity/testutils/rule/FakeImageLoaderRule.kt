package com.example.catsonactivity.testutils.rule

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import coil.Coil
import coil.ImageLoader
import com.example.catsonactivity.testutils.FakeImageLoader
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * This test rule installs a [FakeImageLoader] instead of a real one
 * into the Coil library.
 * @see FakeImageLoader
 */


class FakeImageLoaderRule : TestWatcher() {

    override fun starting(description: Description) {
        super.starting(description)
        Coil.setImageLoader(FakeImageLoader())
    }

    override fun finished(description: Description) {
        super.finished(description)
        val defaultLoader = ImageLoader(ApplicationProvider.getApplicationContext< Application>())
        Coil.setImageLoader(defaultLoader)
    }
}