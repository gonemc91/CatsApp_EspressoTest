package com.example.catsonactivity.testutils.espresso

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.test.core.app.ApplicationProvider
import org.hamcrest.Description
import org.hamcrest.Matcher


/**
 * Check that the image view shows the specified drawable and
 * optionally the drawable is tinned with the [tintColorRes]
 */

fun withDrawable(
    drawable: Drawable,
    @ColorRes tintColorRes: Int? = null
): org.hamcrest.Matcher<View> = WithDrawable(drawable, tintColorRes)

/**
 * Check that the image view shows the specified drawable resource and
 * optionally the drawable is tinned with the [tintColorRes]
 */

fun withDrawable(
    @DrawableRes drawableRes: Int,
    @ColorRes tintColorRes: Int? = null
): Matcher<View> = WithDrawable(drawableRes, tintColorRes)


private class WithDrawable(
    private val drawable: Drawable,
    @ColorRes private val tintColorRes: Int?
): org.hamcrest.TypeSafeMatcher<View> (){

    constructor(
        @DrawableRes drawableRes: Int,
        @ColorRes tintColorRes: Int?
    ) : this(
        ContextCompat.getDrawable(
            ApplicationProvider.getApplicationContext(),
            drawableRes
        )!!,
        tintColorRes
    )
//for looking in Logcat
    override fun describeTo(description: Description?) {
        description?.appendText("Image view the specified drawable res.")
    }
//check this image.
    override fun matchesSafely(item: View?): Boolean {
        if (item !is ImageView) return false
        if(tintColorRes != null){
            drawable.setTintList(
                ColorStateList.valueOf(
                    ContextCompat.getColor(item.context, tintColorRes)
                )
            )
        }
        return item.drawable.toBitmap().sameAs(drawable.toBitmap())
    }
}