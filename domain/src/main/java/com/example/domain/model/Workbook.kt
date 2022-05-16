package com.example.domain.model

import com.example.core.TestMakerColor

data class Workbook(
    val id: WorkbookId,
    val remoteId: String,
    val name: String,
    val color: TestMakerColor,
    val order: Int,
    val folderName: String,
    val questionList: List<Question>
)

@JvmInline
value class WorkbookId(val value: Long)
