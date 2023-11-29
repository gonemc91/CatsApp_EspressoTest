package com.example.catsonactivity.apps.di

import com.example.catsonactivity.model.CatsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.mockk.mockk
import javax.inject.Singleton


/**
 * This module replaces the real repository by a fake repository.
 * You need to uninstall the real module by using [UninstallModules]
 * annotation in all your test classes.
 */

@Module
@InstallIn(SingletonComponent::class)
class FakerRepositoriesModule {
    @Provides
    @Singleton
    fun providesCatsRepository(): CatsRepository{
        return mockk()
    }
}