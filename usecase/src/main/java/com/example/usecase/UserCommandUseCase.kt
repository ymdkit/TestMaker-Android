package com.example.usecase

import com.example.domain.model.FolderId
import com.example.domain.model.WorkbookId
import com.example.domain.repository.WorkBookRepository
import com.example.usecase.model.FolderUseCaseModel
import com.example.usecase.model.WorkbookUseCaseModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserCommandUseCase @Inject constructor(
    private val workBookRepository: WorkBookRepository
) {

    suspend fun updateFolder(folder: FolderUseCaseModel, newFolderName: String) {
        val workbookList = workBookRepository.getWorkbookListByFolderName(folder.name)
        workBookRepository.updateFolder(folder.copy(name = newFolderName).toFolder())
        workbookList.forEach {
            val newWorkbook = it.copy(folderName = newFolderName)
            workBookRepository.updateWorkbook(newWorkbook)
        }
    }

    suspend fun deleteFolder(folder: FolderUseCaseModel) {
        val folderName = folder.name
        val workbookList = workBookRepository.getWorkbookListByFolderName(folderName)
        workBookRepository.deleteFolder(FolderId(folder.id))
        workbookList.forEach {
            workBookRepository.deleteWorkbook(it.id)
        }
    }

    suspend fun deleteWorkbook(workbook: WorkbookUseCaseModel) =
        workBookRepository.deleteWorkbook(WorkbookId(workbook.id))
}
