package com.example.domain.repository

import com.example.domain.model.DocumentId
import com.example.domain.model.GroupId
import com.example.domain.model.SharedWorkbook
import com.example.domain.model.UserId
import kotlinx.coroutines.flow.Flow

interface SharedWorkbookRepository {

    val updateWorkbookListFlow: Flow<List<SharedWorkbook>>
    val updateGroupWorkbookListFlow: Flow<List<SharedWorkbook>>

    suspend fun getWorkbookListByUserId(userId: UserId): List<SharedWorkbook>
    suspend fun getWorkbookListByGroupId(groupId: GroupId): List<SharedWorkbook>
    suspend fun createWorkbook(workbook: SharedWorkbook)
    suspend fun deleteWorkbook(workbookId: DocumentId)
}