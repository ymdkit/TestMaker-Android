package com.example.domain.model

data class Workbook(
    val id: WorkbookId,
    val name: String,
    val color: Int, // todo don't use resId directly
    val folderName: String,
    val questionList: List<Question>
)

@JvmInline
value class WorkbookId(val value: Long)
