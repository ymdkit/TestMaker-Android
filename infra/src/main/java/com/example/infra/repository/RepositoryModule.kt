package com.example.infra.repository

import com.example.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Singleton
    @Binds
    abstract fun bindWorkbookModule(
        workbookRepositoryImpl: WorkbookRepositoryImpl
    ): WorkBookRepository

    @Singleton
    @Binds
    abstract fun bindSharedWorkbookModule(
        sharedWorkbookRepositoryImpl: SharedWorkbookRepositoryImpl
    ): SharedWorkbookRepository

    @Singleton
    @Binds
    abstract fun bindPreferenceRepository(
        preferenceRepositoryImpl: PreferenceRepositoryImpl
    ): PreferenceRepository

    @Singleton
    @Binds
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Singleton
    @Binds
    abstract fun bindGroupRepository(
        groupRepositoryImpl: GroupRepositoryImpl
    ): GroupRepository

}
