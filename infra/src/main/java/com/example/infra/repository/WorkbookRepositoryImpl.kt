package com.example.infra.repository

import com.example.domain.model.Workbook
import com.example.domain.model.WorkbookId
import com.example.domain.repository.WorkBookRepository
import com.example.infra.local.db.WorkbookDataSource
import com.example.infra.local.entity.RealmTest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkbookRepositoryImpl @Inject constructor(
    private val db: WorkbookDataSource,
) : WorkBookRepository {

    private val _createWorkbookFlow: MutableSharedFlow<Workbook> =
        MutableSharedFlow()
    override val createWorkbookFlow: Flow<Workbook>
        get() = _createWorkbookFlow

    private val _updateWorkbookFlow: MutableSharedFlow<Workbook> =
        MutableSharedFlow()
    override val updateWorkbookFlow: Flow<Workbook>
        get() = _updateWorkbookFlow

    private val _deleteWorkbookFlow: MutableSharedFlow<WorkbookId> =
        MutableSharedFlow()
    override val deleteWorkbookFlow: Flow<WorkbookId>
        get() = _deleteWorkbookFlow

    override suspend fun getWorkbookList(): List<Workbook> =
        db.getWorkbookList().map {
            it.toWorkbook()
        }

    override suspend fun getWorkbook(workbookId: WorkbookId): Workbook =
        db.getWorkbook(id = workbookId.value).toWorkbook()

    override suspend fun createWorkbook(workbook: Workbook) {
        db.createWorkbook(RealmTest.fromWorkbook(workbook))
        _createWorkbookFlow.emit(workbook)
    }

    override suspend fun updateWorkbook(workbook: Workbook) {
        db.createWorkbook(RealmTest.fromWorkbook(workbook))
        _updateWorkbookFlow.emit(workbook)
    }

    override suspend fun deleteWorkbook(workbookId: WorkbookId) {
        db.deleteWorkbook(workbookId.value)
        _deleteWorkbookFlow.emit(workbookId)
    }

}