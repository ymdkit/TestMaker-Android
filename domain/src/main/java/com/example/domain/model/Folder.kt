package com.example.domain.model

data class Folder(
    val id: FolderId,
    val name: String,
    val workbookList: List<Workbook>
)

@JvmInline
value class FolderId(val value: Long)
