package com.example.usecase.model

import com.example.domain.model.Folder
import com.example.domain.model.FolderId

data class FolderUseCaseModel(
    val id: Long,
    val name: String,
    val color: Int,
    val order: Int,
    val workbookCount: Int
) {
    companion object {
        fun fromFolder(
            folder: Folder
        ): FolderUseCaseModel =
            FolderUseCaseModel(
                id = folder.id.value,
                name = folder.name,
                color = folder.color,
                order = folder.order,
                workbookCount = folder.workbookCount
            )
    }

    fun toFolder() = Folder(
        id = FolderId(id),
        name = name,
        color = color,
        order = order,
        workbookCount = workbookCount,
    )
}
