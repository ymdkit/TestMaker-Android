package com.example.domain.model

data class AnswerHistory(
    val id: DocumentId,
    val userId: UserId,
    val userName: String,
    val createdAt: String,
    val numCorrect: Int,
    val numSolved: Int
)
