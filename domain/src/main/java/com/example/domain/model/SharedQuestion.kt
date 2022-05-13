package com.example.domain.model

import com.example.core.QuestionType

data class SharedQuestion(
    val id: DocumentId,
    val problem: String,
    val explanation: String,
    val answerList: List<String>,
    val otherSelectionList: List<String>,
    val problemImageUrl: String,
    val explanationImageUrl: String,
    val questionType: QuestionType,
    val isCheckAnswerOrder: Boolean,
    val isAutoGenerateOtherSelections: Boolean,
    val order: Int
)
