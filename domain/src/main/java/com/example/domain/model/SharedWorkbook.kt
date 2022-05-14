package com.example.domain.model

data class SharedWorkbook(
    val id: DocumentId,
    val name: String,
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
