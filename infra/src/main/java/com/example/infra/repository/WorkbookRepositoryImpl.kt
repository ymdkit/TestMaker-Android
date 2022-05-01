package com.example.infra.repository

import com.example.domain.model.Workbook
import com.example.domain.model.WorkbookId
import com.example.domain.repository.WorkBookRepository
import com.example.infra.local.db.WorkbookDataSource
import com.example.infra.local.entity.RealmTest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkbookRepositoryImpl @Inject constructor(
    private val db: WorkbookDataSource,
) : WorkBookRepository {

    override fun getWorkbookList(): List<Workbook> =
        db.getWorkbookList().map {
            it.toWorkbook()
        }

    override fun getWorkbook(workbookId: WorkbookId): Workbook =
        db.getWorkbook(id = workbookId.value).toWorkbook()

    override fun createWorkbook(workbook: Workbook) =
        db.createWorkbook(RealmTest.fromWorkbook(workbook))

    override fun updateWorkbook(workbook: Workbook) =
        db.createWorkbook(RealmTest.fromWorkbook(workbook))

    override fun deleteWorkbook(workbook: Workbook) =
        db.deleteWorkbook(RealmTest.fromWorkbook(workbook))

}