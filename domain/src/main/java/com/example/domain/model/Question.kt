package com.example.domain.model

sealed class Question {
    abstract val id: Long
    abstract val problem: String
    abstract val answer: String
    abstract val explanation: String
    abstract val problemImageUrl: String
    abstract val explanationImageUrl: String
    abstract val answerStatus: AnswerStatus
    abstract val isAnswering: Boolean
    abstract val order: Int

    data class WriteQuestion(
        override val id: Long,
        override val problem: String,
        override val answer: String,
        override val explanation: String,
        override val problemImageUrl: String,
        override val explanationImageUrl: String,
        override val answerStatus: AnswerStatus,
        override val isAnswering: Boolean,
        override val order: Int
    ) : Question()

    data class SelectQuestion(
        override val id: Long,
        override val problem: String,
        override val answer: String,
        override val explanation: String,
        override val problemImageUrl: String,
        override val explanationImageUrl: String,
        override val answerStatus: AnswerStatus,
        override val isAnswering: Boolean,
        override val order: Int,
        val otherSelections: List<String>,
        val isAutoGenerateOtherSelections: Boolean
    ) : Question()

    data class CompleteQuestion(
        override val id: Long,
        override val problem: String,
        override val answer: String,
        override val explanation: String,
        override val problemImageUrl: String,
        override val explanationImageUrl: String,
        override val answerStatus: AnswerStatus,
        override val isAnswering: Boolean,
        override val order: Int,
        val answers: List<String>,
        val isCheckAnswerOrder: Boolean
    ) : Question()

    data class SelectCompleteQuestion(
        override val id: Long,
        override val problem: String,
        override val answer: String,
        override val explanation: String,
        override val problemImageUrl: String,
        override val explanationImageUrl: String,
        override val answerStatus: AnswerStatus,
        override val isAnswering: Boolean,
        override val order: Int,
        val answers: List<String>,
        val otherSelections: List<String>,
        val isAutoGenerateOtherSelections: Boolean,
        val isCheckAnswerOrder: Boolean
    ) : Question()
}

enum class AnswerStatus {
    CORRECT,
    INCORRECT,
    UNANSWERED
}
