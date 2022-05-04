package com.example.usecase.model

import com.example.domain.model.AnswerStatus
import com.example.domain.model.Question

sealed class QuestionUseCaseModel {
    abstract val id: Long
    abstract val problem: String
    abstract val answers: List<String>
    abstract val explanation: String
    abstract val problemImageUrl: String
    abstract val explanationImageUrl: String
    abstract val answerStatus: AnswerStatus
    abstract val isAnswering: Boolean
    abstract val order: Int

    fun getSingleLineAnswer() = answers.joinToString(" ")

    data class WriteQuestionUseCaseModel(
        override val id: Long,
        override val problem: String,
        override val answers: List<String>,
        override val explanation: String,
        override val problemImageUrl: String,
        override val explanationImageUrl: String,
        override val answerStatus: AnswerStatus,
        override val isAnswering: Boolean,
        override val order: Int
    ) : QuestionUseCaseModel()

    data class SelectQuestionUseCaseModel(
        override val id: Long,
        override val problem: String,
        override val answers: List<String>,
        override val explanation: String,
        override val problemImageUrl: String,
        override val explanationImageUrl: String,
        override val answerStatus: AnswerStatus,
        override val isAnswering: Boolean,
        override val order: Int,
        val otherSelections: List<String>,
        val isAutoGenerateOtherSelections: Boolean
    ) : QuestionUseCaseModel()

    data class CompleteQuestionUseCaseModel(
        override val id: Long,
        override val problem: String,
        override val answers: List<String>,
        override val explanation: String,
        override val problemImageUrl: String,
        override val explanationImageUrl: String,
        override val answerStatus: AnswerStatus,
        override val isAnswering: Boolean,
        override val order: Int,
        val isCheckAnswerOrder: Boolean
    ) : QuestionUseCaseModel()

    data class SelectCompleteQuestionUseCaseModel(
        override val id: Long,
        override val problem: String,
        override val answers: List<String>,
        override val explanation: String,
        override val problemImageUrl: String,
        override val explanationImageUrl: String,
        override val answerStatus: AnswerStatus,
        override val isAnswering: Boolean,
        override val order: Int,
        val otherSelections: List<String>,
        val isAutoGenerateOtherSelections: Boolean,
        val isCheckAnswerOrder: Boolean
    ) : QuestionUseCaseModel()

    companion object {
        fun fromQuestion(question: Question): QuestionUseCaseModel =
            when (question) {
                is Question.WriteQuestion -> WriteQuestionUseCaseModel(
                    id = question.id.value,
                    problem = question.problem,
                    answers = question.answers,
                    explanation = question.explanation,
                    problemImageUrl = question.problemImageUrl,
                    explanationImageUrl = question.explanationImageUrl,
                    answerStatus = question.answerStatus,
                    isAnswering = question.isAnswering,
                    order = question.order
                )
                is Question.SelectQuestion -> SelectQuestionUseCaseModel(
                    id = question.id.value,
                    problem = question.problem,
                    answers = question.answers,
                    explanation = question.explanation,
                    problemImageUrl = question.problemImageUrl,
                    explanationImageUrl = question.explanationImageUrl,
                    answerStatus = question.answerStatus,
                    isAnswering = question.isAnswering,
                    order = question.order,
                    otherSelections = question.otherSelections,
                    isAutoGenerateOtherSelections = question.isAutoGenerateOtherSelections
                )
                is Question.CompleteQuestion -> CompleteQuestionUseCaseModel(
                    id = question.id.value,
                    problem = question.problem,
                    answers = question.answers,
                    explanation = question.explanation,
                    problemImageUrl = question.problemImageUrl,
                    explanationImageUrl = question.explanationImageUrl,
                    answerStatus = question.answerStatus,
                    isAnswering = question.isAnswering,
                    order = question.order,
                    isCheckAnswerOrder = question.isCheckAnswerOrder
                )
                is Question.SelectCompleteQuestion -> SelectCompleteQuestionUseCaseModel(
                    id = question.id.value,
                    problem = question.problem,
                    answers = question.answers,
                    explanation = question.explanation,
                    problemImageUrl = question.problemImageUrl,
                    explanationImageUrl = question.explanationImageUrl,
                    answerStatus = question.answerStatus,
                    isAnswering = question.isAnswering,
                    order = question.order,
                    otherSelections = question.otherSelections,
                    isAutoGenerateOtherSelections = question.isAutoGenerateOtherSelections,
                    isCheckAnswerOrder = question.isCheckAnswerOrder
                )
            }
    }
}
