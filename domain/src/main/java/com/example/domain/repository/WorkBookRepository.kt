package com.example.domain.repository

import com.example.domain.model.Folder
import com.example.domain.model.FolderId
import com.example.domain.model.Workbook
import com.example.domain.model.WorkbookId
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
    suspend fun createWorkbook(name: String, color: Int, folderName: String)
    suspend fun updateWorkbook(workbook: Workbook)
    suspend fun deleteWorkbook(workbookId: WorkbookId)
    suspend fun swapWorkbook(sourceWorkbook: Workbook, destWorkbook: Workbook)

    suspend fun getWorkbookListByFolderName(folderName: String): List<Workbook>
}