package com.example.domain.model

data class User(
    val id: UserId,
    val displayName: String,
)

@JvmInline
value class UserId(
    val value: String
)
