package com.example.domain.model

import com.example.core.TestMakerColor

data class SharedWorkbook(
    val id: DocumentId,
    val name: String,
    val color: TestMakerColor,
    val userId: UserId,
    val userName: String,
    val comment: String,
    val questionListCount: Int,
    val downloadCount: Int,
    val isPublic: Boolean,
    val groupId: GroupId?
)

@JvmInline
value class DocumentId(
    val value: String
)
