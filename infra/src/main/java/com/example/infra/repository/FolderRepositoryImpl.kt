package com.example.infra.repository

import com.example.domain.model.Folder
import com.example.domain.model.FolderId
import com.example.domain.repository.FolderRepository
import com.example.infra.local.db.FolderDataSource
import com.example.infra.local.entity.RealmCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FolderRepositoryImpl @Inject constructor(
    private val db: FolderDataSource,
) : FolderRepository {

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

    override suspend fun getFolderList(): List<Folder> =
        db.getFolderList().map {
            it.toFolder()
        }

    override suspend fun getFolder(folderId: FolderId): Folder =
        db.getFolder(folderId = folderId.value).toFolder()

    override suspend fun createFolder(folder: Folder) {
        db.createFolder(RealmCategory.fromFolder(folder))
        _createFolderFlow.emit(folder)
    }

    override suspend fun updateFolder(folder: Folder) {
        db.createFolder(RealmCategory.fromFolder(folder))
        _updateFolderFlow.emit(folder)
    }

    override suspend fun deleteFolder(folderId: FolderId) {
        db.deleteFolder(folderId.value)
        _deleteFolderFlow.emit(folderId)
    }

}