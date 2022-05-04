package com.example.domain.model

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
            when (question) {
                is Question.WriteQuestion -> CreateQuestionRequest(
                    questionType = QuestionType.WRITE,
                    problem = question.problem,
                    answers = question.answers,
                    explanation = question.explanation,
                    problemImageUrl = question.problemImageUrl,
                    explanationImageUrl = question.explanationImageUrl,
                    otherSelections = listOf(),
                    isAutoGenerateOtherSelections = false,
                    isCheckAnswerOrder = false
                )
                is Question.SelectQuestion -> CreateQuestionRequest(
                    questionType = QuestionType.SELECT,
                    problem = question.problem,
                    answers = question.answers,
                    explanation = question.explanation,
                    problemImageUrl = question.problemImageUrl,
                    explanationImageUrl = question.explanationImageUrl,
                    otherSelections = question.otherSelections,
                    isAutoGenerateOtherSelections = question.isAutoGenerateOtherSelections,
                    isCheckAnswerOrder = false
                )
                is Question.CompleteQuestion -> CreateQuestionRequest(
                    questionType = QuestionType.COMPLETE,
                    problem = question.problem,
                    answers = question.answers,
                    explanation = question.explanation,
                    problemImageUrl = question.problemImageUrl,
                    explanationImageUrl = question.explanationImageUrl,
                    otherSelections = listOf(),
                    isAutoGenerateOtherSelections = false,
                    isCheckAnswerOrder = question.isCheckAnswerOrder
                )
                is Question.SelectCompleteQuestion -> CreateQuestionRequest(
                    questionType = QuestionType.SELECT_COMPLETE,
                    problem = question.problem,
                    answers = question.answers,
                    explanation = question.explanation,
                    problemImageUrl = question.problemImageUrl,
                    explanationImageUrl = question.explanationImageUrl,
                    otherSelections = question.otherSelections,
                    isAutoGenerateOtherSelections = question.isAutoGenerateOtherSelections,
                    isCheckAnswerOrder = question.isCheckAnswerOrder
                )
            }
    }
}

enum class QuestionType(val value: Int) {
    WRITE(0),
    SELECT(1),
    COMPLETE(2),
    SELECT_COMPLETE(3);
}
