package com.example.domain.repository

import com.example.domain.model.Folder
import com.example.domain.model.FolderId
import kotlinx.coroutines.flow.Flow

interface FolderRepository {

    val createFolderFlow: Flow<Folder>
    val updateFolderFlow: Flow<Folder>
    val deleteFolderFlow: Flow<FolderId>

    suspend fun getFolderList(): List<Folder>
    suspend fun getFolder(folderId: FolderId): Folder
    suspend fun createFolder(folder: Folder)
    suspend fun updateFolder(folder: Folder)
    suspend fun deleteFolder(folderId: FolderId)
}