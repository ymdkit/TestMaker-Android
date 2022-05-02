package com.example.domain.model

data class Folder(
    val id: FolderId,
    val name: String,
    val color: Int,
    val order: Int,
    val workbookCount: Int
)

@JvmInline
value class FolderId(val value: Long)
