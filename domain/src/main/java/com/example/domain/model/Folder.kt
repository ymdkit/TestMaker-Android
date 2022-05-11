package com.example.domain.model

import com.example.core.TestMakerColor

data class Folder(
    val id: FolderId,
    val name: String,
    val color: TestMakerColor,
    val order: Int,
    val workbookCount: Int
)

@JvmInline
value class FolderId(val value: Long)
