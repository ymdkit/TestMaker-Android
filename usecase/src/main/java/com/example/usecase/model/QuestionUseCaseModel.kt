package com.example.usecase.model

import com.example.core.AnswerStatus
import com.example.core.QuestionType
import com.example.domain.model.Question
import com.example.domain.model.QuestionId

data class QuestionUseCaseModel(
    val id: Long,
    val type: QuestionType,
    val problem: String,
    val answers: List<String>,
    val explanation: String,
    val problemImageUrl: String,
    val explanationImageUrl: String,
    val answerStatus: AnswerStatus,
    val isAnswering: Boolean,
    val order: Int,
    val otherSelections: List<String>,
    val isAutoGenerateOtherSelections: Boolean,
    val isCheckAnswerOrder: Boolean
) {
    fun getSingleLineAnswer() = answers.joinToString(" ")
    fun getMultipleLineAnswer() = answers.joinToString("\n")
    fun getSwappableAnswers(isSwap: Boolean) = if (isSwap) listOf(problem) else answers

    companion object {
        fun fromQuestion(question: Question): QuestionUseCaseModel =
            QuestionUseCaseModel(
                id = question.id.value,
                type = question.type,
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

    fun toQuestion() =
        Question(
            id = QuestionId(id),
            type = type,
            problem = problem,
            answers = answers,
            explanation = explanation,
            problemImageUrl = problemImageUrl,
            explanationImageUrl = explanationImageUrl,
            answerStatus = answerStatus,
            isAnswering = isAnswering,
            order = order,
            otherSelections = otherSelections,
            isAutoGenerateOtherSelections = isAutoGenerateOtherSelections,
            isCheckAnswerOrder = isCheckAnswerOrder
        )

}