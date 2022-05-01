package com.example.domain.repository

import com.example.domain.model.Workbook
import com.example.domain.model.WorkbookId

interface WorkBookRepository {
    fun getWorkbookList(): List<Workbook>
    fun getWorkbook(workbookId: WorkbookId): Workbook
    fun createWorkbook(workbook: Workbook)
    fun updateWorkbook(workbook: Workbook)
    fun deleteWorkbook(workbook: Workbook)
}