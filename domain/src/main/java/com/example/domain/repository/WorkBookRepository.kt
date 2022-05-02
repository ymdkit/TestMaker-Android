package com.example.domain.repository

import com.example.domain.model.Workbook
import com.example.domain.model.WorkbookId
import kotlinx.coroutines.flow.Flow

interface WorkBookRepository {

    val createWorkbookFlow: Flow<Workbook>
    val updateWorkbookFlow: Flow<Workbook>
    val deleteWorkbookFlow: Flow<WorkbookId>

    suspend fun getWorkbookList(): List<Workbook>
    suspend fun getWorkbook(workbookId: WorkbookId): Workbook
    suspend fun createWorkbook(workbook: Workbook)
    suspend fun updateWorkbook(workbook: Workbook)
    suspend fun deleteWorkbook(workbookId: WorkbookId)
}