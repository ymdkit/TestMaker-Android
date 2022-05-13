package com.example.domain.model

data class SharedWorkbook(
    val id: DocumentId,
    val name: String,
    val userId: UserId,
    val userName: String,
    val questionListCount: Int,
    val downloadCount: Int,
    val isPublic: Boolean,
    val groupId: String?
)

@JvmInline
value class DocumentId(
    val value: String
)
