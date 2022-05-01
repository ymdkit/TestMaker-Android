package com.example.domain.model

data class Folder(
    val id: Long,
    val name: String,
    val workbookList: List<Workbook>
)