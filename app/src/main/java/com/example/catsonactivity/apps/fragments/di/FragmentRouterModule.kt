package com.example.catsonactivity.apps.fragments.di

import androidx.fragment.app.FragmentActivity
import com.example.catsonactivity.apps.fragments.FragmentRouter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
class FragmentRouterModule {

    @Provides
    fun bindRouter(activity: FragmentActivity): FragmentRouter {
        return activity as FragmentRouter
    }
}