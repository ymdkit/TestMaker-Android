package com.example.infra.repository

import com.example.domain.repository.WorkBookRepository
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

}
