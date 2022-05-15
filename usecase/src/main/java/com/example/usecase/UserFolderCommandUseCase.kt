package com.example.usecase

import com.example.core.TestMakerColor
import com.example.domain.model.FolderId
import com.example.domain.repository.WorkBookRepository
import com.example.usecase.model.FolderUseCaseModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserFolderCommandUseCase @Inject constructor(
    private val workBookRepository: WorkBookRepository
) {

    suspend fun createFolder(name: String, color: TestMakerColor) =
        workBookRepository.createFolder(name, color)

    suspend fun updateFolder(folder: FolderUseCaseModel) {
        val workbookList = workBookRepository.getWorkbookListByFolderName(folder.name)
        workbookList.forEach {
            val newWorkbook = it.copy(folderName = folder.name)
            workBookRepository.updateWorkbook(newWorkbook)
        }
        workBookRepository.updateFolder(folder.toFolder())
    }

    suspend fun deleteFolder(folder: FolderUseCaseModel) {
        val folderName = folder.name
        val workbookList = workBookRepository.getWorkbookListByFolderName(folderName)
        workBookRepository.deleteFolder(FolderId(folder.id))
        workbookList.forEach {
            workBookRepository.deleteWorkbook(it.id)
        }
    }

    suspend fun swapFolder(sourceFolderId: Long, destFolderId: Long) {
        val sourceFolder = workBookRepository.getFolder(FolderId(sourceFolderId))
        val destFolder = workBookRepository.getFolder(FolderId(destFolderId))
        workBookRepository.updateFolder(sourceFolder.copy(order = destFolder.order))
        workBookRepository.updateFolder(destFolder.copy(order = sourceFolder.order))
    }
}
