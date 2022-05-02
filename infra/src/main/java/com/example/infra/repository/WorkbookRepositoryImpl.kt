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

    private val _updateFolderListFlow: MutableSharedFlow<List<Folder>> =
        MutableSharedFlow()
    override val updateFolderListFlow: Flow<List<Folder>>
        get() = _updateFolderListFlow

    private val _updateWorkbookListFlow: MutableSharedFlow<List<Workbook>> =
        MutableSharedFlow()
    override val updateWorkBookListFlow: Flow<List<Workbook>>
        get() = _updateWorkbookListFlow

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
        refreshFolderList()
    }

    override suspend fun updateFolder(folder: Folder) {
        folderDataSource.createFolder(RealmCategory.fromFolder(folder))
        refreshFolderList()
    }

    override suspend fun deleteFolder(folderId: FolderId) {
        folderDataSource.deleteFolder(folderId.value)
        refreshFolderList()
    }

    override suspend fun getWorkbookList(): List<Workbook> =
        workbookDataSource.getWorkbookList().map {
            it.toWorkbook()
        }

    override suspend fun getWorkbook(workbookId: WorkbookId): Workbook =
        workbookDataSource.getWorkbook(id = workbookId.value).toWorkbook()

    override suspend fun createWorkbook(
        name: String,
        color: Int,
        folderName: String
    ) {
        val workbookId = workbookDataSource.generateWorkbookId()
        val newWorkbook = Workbook(
            id = WorkbookId(workbookId),
            name = name,
            color = color,
            folderName = folderName,
            order = workbookId.toInt(),
            questionList = listOf()
        )
        workbookDataSource.createWorkbook(RealmTest.fromWorkbook(newWorkbook))
        refreshWorkbookList()
    }

    override suspend fun updateWorkbook(workbook: Workbook) {
        workbookDataSource.createWorkbook(RealmTest.fromWorkbook(workbook))
        refreshWorkbookList()
    }

    override suspend fun deleteWorkbook(workbookId: WorkbookId) {
        workbookDataSource.deleteWorkbook(workbookId.value)
        refreshWorkbookList()
    }

    override suspend fun swapWorkbook(sourceWorkbook: Workbook, destWorkbook: Workbook) {
        workbookDataSource.createWorkbook(RealmTest.fromWorkbook(sourceWorkbook.copy(order = destWorkbook.order)))
        workbookDataSource.createWorkbook(RealmTest.fromWorkbook(destWorkbook.copy(order = sourceWorkbook.order)))
        refreshWorkbookList()
    }

    override suspend fun getWorkbookListByFolderName(folderName: String): List<Workbook> =
        workbookDataSource.getWorkbookList()
            .filter { workbook -> workbook.getCategory() == folderName }
            .map { it.toWorkbook() }

    private suspend fun refreshWorkbookList() {
        _updateWorkbookListFlow.emit(getWorkbookList())
    }

    private suspend fun refreshFolderList() {
        _updateFolderListFlow.emit(getFolderList())
    }
}