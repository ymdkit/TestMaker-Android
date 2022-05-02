package com.example.infra.repository

import com.example.domain.model.Folder
import com.example.domain.model.FolderId
import com.example.domain.model.Workbook
import com.example.domain.model.WorkbookId
import com.example.domain.repository.WorkBookRepository
import com.example.infra.local.db.FolderDataSource
import com.example.infra.local.db.WorkbookDataSource
import com.example.infra.local.entity.RealmCategory
import com.example.infra.local.entity.RealmTest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkbookRepositoryImpl @Inject constructor(
    private val workbookDataSource: WorkbookDataSource,
    private val folderDataSource: FolderDataSource,
) : WorkBookRepository {

    private val _createFolderFlow: MutableSharedFlow<Folder> =
        MutableSharedFlow()
    override val createFolderFlow: Flow<Folder>
        get() = _createFolderFlow

    private val _updateFolderFlow: MutableSharedFlow<Folder> =
        MutableSharedFlow()
    override val updateFolderFlow: Flow<Folder>
        get() = _updateFolderFlow

    private val _deleteFolderFlow: MutableSharedFlow<FolderId> =
        MutableSharedFlow()
    override val deleteFolderFlow: Flow<FolderId>
        get() = _deleteFolderFlow

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

    override suspend fun getFolderList(): List<Folder> {
        return folderDataSource.getFolderList().map {
            val workbookCount = getWorkbookListByFolderName(folderName = it.name).count()
            it.toFolder(workbookCount = workbookCount)
        }
    }

    override suspend fun getFolder(folderId: FolderId): Folder {
        val realmFolder = folderDataSource.getFolder(folderId = folderId.value)
        val workbookCount = getWorkbookListByFolderName(folderName = realmFolder.name).count()
        return realmFolder.toFolder(workbookCount = workbookCount)
    }

    override suspend fun createFolder(folder: Folder) {
        folderDataSource.createFolder(RealmCategory.fromFolder(folder))
        _createFolderFlow.emit(folder)
    }

    override suspend fun updateFolder(folder: Folder) {
        folderDataSource.createFolder(RealmCategory.fromFolder(folder))
        _updateFolderFlow.emit(folder)
    }

    override suspend fun deleteFolder(folderId: FolderId) {
        folderDataSource.deleteFolder(folderId.value)
        _deleteFolderFlow.emit(folderId)
    }

    override suspend fun getWorkbookList(): List<Workbook> =
        workbookDataSource.getWorkbookList().map {
            it.toWorkbook()
        }

    override suspend fun getWorkbook(workbookId: WorkbookId): Workbook =
        workbookDataSource.getWorkbook(id = workbookId.value).toWorkbook()

    override suspend fun createWorkbook(workbook: Workbook) {
        this.workbookDataSource.createWorkbook(RealmTest.fromWorkbook(workbook))
        _createWorkbookFlow.emit(workbook)
    }

    override suspend fun updateWorkbook(workbook: Workbook) {
        this.workbookDataSource.createWorkbook(RealmTest.fromWorkbook(workbook))
        _updateWorkbookFlow.emit(workbook)
    }

    override suspend fun deleteWorkbook(workbookId: WorkbookId) {
        workbookDataSource.deleteWorkbook(workbookId.value)
        _deleteWorkbookFlow.emit(workbookId)
    }

    override suspend fun getWorkbookListByFolderName(folderName: String): List<Workbook> =
        workbookDataSource.getWorkbookList()
            .filter { workbook -> workbook.getCategory() == folderName }
            .map { it.toWorkbook() }
}