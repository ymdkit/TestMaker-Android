package com.example.domain.model

data class Group(
    val id: GroupId,
    val name: String,
    val userId: UserId,
    val createdAt: String
)

@JvmInline
value class GroupId(
    val value: String
)
