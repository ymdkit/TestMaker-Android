package com.example.domain.model

import com.example.core.QuestionType

data class CreateQuestionRequest(
    val questionType: QuestionType,
    val problem: String,
    val answers: List<String>,
    val explanation: String,
    val problemImageUrl: String,
    val explanationImageUrl: String,
    val otherSelections: List<String>,
    val isAutoGenerateOtherSelections: Boolean,
    val isCheckAnswerOrder: Boolean
) {
    companion object {
        fun fromQuestion(question: Question) =
            CreateQuestionRequest(
                questionType = question.type,
                problem = question.problem,
                answers = question.answers,
                explanation = question.explanation,
                problemImageUrl = question.problemImageUrl,
                explanationImageUrl = question.explanationImageUrl,
                otherSelections = question.otherSelections,
                isAutoGenerateOtherSelections = question.isAutoGenerateOtherSelections,
                isCheckAnswerOrder = question.isCheckAnswerOrder
            )

        fun fromSharedQuestion(question: SharedQuestion) =
            CreateQuestionRequest(
                questionType = question.questionType,
                problem = question.problem,
                answers = question.answerList,
                explanation = question.explanation,
                problemImageUrl = question.problemImageUrl,
                explanationImageUrl = question.explanationImageUrl,
                otherSelections = question.otherSelectionList,
                isAutoGenerateOtherSelections = question.isAutoGenerateOtherSelections,
                isCheckAnswerOrder = question.isCheckAnswerOrder
            )
    }
}

