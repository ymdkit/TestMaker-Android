package com.example.usecase

import com.example.domain.model.AnswerStatus
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

    suspend fun createFolder(name: String, color: Int) =
        workBookRepository.createFolder(name, color)

    suspend fun updateFolder(folder: FolderUseCaseModel, newFolderName: String) {
        val workbookList = workBookRepository.getWorkbookListByFolderName(folder.name)
        workbookList.forEach {
            val newWorkbook = it.copy(folderName = newFolderName)
            workBookRepository.updateWorkbook(newWorkbook)
        }
        workBookRepository.updateFolder(folder.copy(name = newFolderName).toFolder())
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

    suspend fun createWorkbook(name: String, color: Int, folderName: String) =
        workBookRepository.createWorkbook(name, color, folderName)

    suspend fun updateWorkbook(workbookId: Long, name: String, color: Int, folderName: String) {
        val workbook = workBookRepository.getWorkbook(workbookId = WorkbookId(workbookId))
        workBookRepository.updateWorkbook(
            workbook.copy(
                name = name,
                color = color,
                folderName = folderName
            )
        )
    }


    suspend fun deleteWorkbook(workbook: WorkbookUseCaseModel) =
        workBookRepository.deleteWorkbook(WorkbookId(workbook.id))

    suspend fun swapWorkbook(sourceWorkbookId: Long, destWorkbookId: Long) {
        val sourceWorkbook = workBookRepository.getWorkbook(WorkbookId(sourceWorkbookId))
        val destWorkbook = workBookRepository.getWorkbook(WorkbookId(destWorkbookId))
        workBookRepository.swapWorkbook(sourceWorkbook, destWorkbook)
    }

    suspend fun resetWorkbookAchievement(workbookId: Long) {
        val workbook = workBookRepository.getWorkbook(WorkbookId(workbookId))
        val newQuestionList =
            workbook.questionList.map { it.updated(answerStatus = AnswerStatus.UNANSWERED) }
        workBookRepository.updateWorkbook(workbook.copy(questionList = newQuestionList))
    }
}
