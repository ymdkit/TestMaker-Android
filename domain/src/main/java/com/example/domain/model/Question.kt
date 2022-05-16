package com.example.domain.model

import com.example.core.AnswerStatus
import com.example.core.QuestionImage
import com.example.core.QuestionType

data class Question(
    val id: QuestionId,
    val type: QuestionType,
    val problem: String,
    val answers: List<String>,
    val explanation: String,
    val problemImageUrl: QuestionImage,
    val explanationImageUrl: QuestionImage,
    val answerStatus: AnswerStatus,
    val isAnswering: Boolean,
    val order: Int,
    val otherSelections: List<String>,
    val isAutoGenerateOtherSelections: Boolean,
    val isCheckAnswerOrder: Boolean
)

@JvmInline
value class QuestionId(val value: Long)
