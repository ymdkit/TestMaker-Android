package com.example.domain.repository

import android.net.Uri
import com.example.domain.model.*
import kotlinx.coroutines.flow.Flow

interface SharedWorkbookRepository {

    val updateWorkbookListFlow: Flow<List<SharedWorkbook>>
    val updateGroupWorkbookListFlow: Flow<List<SharedWorkbook>>

    suspend fun getWorkbookList(query: String): List<SharedWorkbook>
    suspend fun getWorkbookListByUserId(userId: UserId): List<SharedWorkbook>
    suspend fun getWorkbookListByGroupId(groupId: GroupId): List<SharedWorkbook>
    suspend fun findWorkbookById(documentId: DocumentId): SharedWorkbook?
    suspend fun createWorkbook(workbook: SharedWorkbook)
    suspend fun deleteWorkbook(userId: UserId, workbookId: DocumentId)
    suspend fun deleteWorkbookFromGroup(groupId: GroupId, workbookId: DocumentId)
    suspend fun shareWorkbook(documentId: DocumentId): Uri
    suspend fun getQuestionListByWorkbookId(documentId: DocumentId): List<SharedQuestion>

}