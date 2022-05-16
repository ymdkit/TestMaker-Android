package com.example.usecase

import com.example.domain.model.WorkbookId
import com.example.domain.repository.WorkBookRepository
import com.example.usecase.model.WorkbookUseCaseModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkbookGetUseCase @Inject constructor(
    private val repository: WorkBookRepository
) {

    suspend fun getWorkbook(workbookId: Long) =
        WorkbookUseCaseModel.fromWorkbook(repository.getWorkbook(workbookId = WorkbookId(workbookId)))
}

