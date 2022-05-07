package com.example.domain.repository

import com.example.domain.model.*
import kotlinx.coroutines.flow.Flow

interface WorkBookRepository {

    val updateFolderListFlow: Flow<List<Folder>>
    val updateWorkBookListFlow: Flow<List<Workbook>>

    suspend fun getFolderList(): List<Folder>
    suspend fun getFolder(folderId: FolderId): Folder
    suspend fun createFolder(name: String, color: Int)
    suspend fun updateFolder(folder: Folder)
    suspend fun deleteFolder(folderId: FolderId)
    suspend fun getWorkbookList(): List<Workbook>
    suspend fun getWorkbook(workbookId: WorkbookId): Workbook
    suspend fun createWorkbook(name: String, remoteId: String, color: Int, folderName: String)
    suspend fun updateWorkbook(workbook: Workbook)
    suspend fun deleteWorkbook(workbookId: WorkbookId)
    suspend fun exportWorkbook(workbook: Workbook): ExportedWorkbook
    suspend fun createQuestion(workbookId: WorkbookId, request: CreateQuestionRequest)
    suspend fun swapWorkbook(sourceWorkbook: Workbook, destWorkbook: Workbook)
    suspend fun getWorkbookListByFolderName(folderName: String): List<Workbook>
    suspend fun getQuestion(questionId: QuestionId): Question
    suspend fun updateQuestion(question: Question)
}