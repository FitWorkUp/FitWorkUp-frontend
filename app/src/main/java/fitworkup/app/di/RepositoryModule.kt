package com.fitworkup.app.di

import com.fitworkup.app.data.repository.ActivityRepositoryImpl
import com.fitworkup.app.data.repository.ProfileRepository
import com.fitworkup.app.data.repository.ProfileRepositoryImpl
import com.fitworkup.app.data.repository.RankingRepositoryImpl
import com.fitworkup.app.domain.repository.ActivityRepository
import com.fitworkup.app.domain.repository.RankingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindActivityRepository(
        activityRepositoryImpl: ActivityRepositoryImpl
    ): ActivityRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        profileRepositoryImpl: ProfileRepositoryImpl
    ): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindRankingRepository(
        rankingRepositoryImpl: RankingRepositoryImpl
    ): RankingRepository
}