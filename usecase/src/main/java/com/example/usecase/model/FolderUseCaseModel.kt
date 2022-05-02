package com.example.usecase.model

import com.example.domain.model.Folder

data class FolderUseCaseModel(
    val id: Long,
    val name: String,
    val color: Int,
    val workbookCount: Int
) {
    companion object {
        fun fromFolder(folder: Folder): FolderUseCaseModel =
            FolderUseCaseModel(
                id = folder.id.value,
                name = folder.name,
                color = folder.color,
                workbookCount = folder.workbookList.count()
            )
    }
}
