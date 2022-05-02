package com.example.usecase

import com.example.domain.model.WorkbookId
import com.example.domain.repository.WorkBookRepository
import com.example.usecase.model.WorkbookUseCaseModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserCommandUseCase @Inject constructor(
    private val workBookRepository: WorkBookRepository
) {
    suspend fun deleteWorkbook(workbook: WorkbookUseCaseModel) =
        workBookRepository.deleteWorkbook(WorkbookId(workbook.id))
}
