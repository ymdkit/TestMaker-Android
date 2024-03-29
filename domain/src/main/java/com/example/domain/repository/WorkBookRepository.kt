package com.example.domain.repository

import com.example.core.TestMakerColor
import com.example.domain.model.*
import kotlinx.coroutines.flow.Flow

interface WorkBookRepository {

    val updateFolderListFlow: Flow<List<Folder>>
    val updateWorkBookListFlow: Flow<List<Workbook>>

    suspend fun getFolderList(): List<Folder>
    suspend fun getFolder(folderId: FolderId): Folder
    suspend fun createFolder(name: String, color: TestMakerColor)
    suspend fun updateFolder(folder: Folder)
    suspend fun deleteFolder(folderId: FolderId)
    suspend fun swapFolder(sourceFolder: Folder, destFolder: Folder)
    suspend fun getWorkbookList(): List<Workbook>
    suspend fun getWorkbook(workbookId: WorkbookId): Workbook
    suspend fun createWorkbook(
        name: String,
        remoteId: String,
        color: TestMakerColor,
        folderName: String
    ): Workbook

    suspend fun updateWorkbook(workbook: Workbook)
    suspend fun deleteWorkbook(workbookId: WorkbookId)
    suspend fun exportWorkbook(workbook: Workbook): ExportedWorkbook
    suspend fun importWorkbook(workbookName: String, exportedWorkbook: ExportedWorkbook): Workbook
    suspend fun createQuestion(workbookId: WorkbookId, request: CreateQuestionRequest)
    suspend fun createQuestionList(workbookId: WorkbookId, requestList: List<CreateQuestionRequest>)
    suspend fun swapWorkbook(sourceWorkbook: Workbook, destWorkbook: Workbook)
    suspend fun getWorkbookListByFolderName(folderName: String): List<Workbook>
    suspend fun getQuestion(questionId: QuestionId): Question
    suspend fun updateQuestion(question: Question)
    suspend fun swapQuestion(sourceQuestion: Question, destQuestion: Question)

}