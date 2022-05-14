package com.example.usecase.model

import com.example.domain.model.AnswerHistory

data class AnswerHistoryUseCaseModel(
    val id: String,
    val userId: String,
    val userName: String,
    val createdAt: String,
    val numCorrect: Int,
    val numSolved: Int
) {

    companion object {
        fun fromAnswerHistory(answerHistory: AnswerHistory) =
            AnswerHistoryUseCaseModel(
                id = answerHistory.id.value,
                userId = answerHistory.userId.value,
                userName = answerHistory.userName,
                createdAt = answerHistory.createdAt,
                numCorrect = answerHistory.numCorrect,
                numSolved = answerHistory.numSolved
            )
    }

}
