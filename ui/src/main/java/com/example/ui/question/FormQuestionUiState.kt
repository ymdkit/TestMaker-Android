package com.example.ui.question

import com.example.core.QuestionImage
import com.example.core.QuestionType
import com.example.usecase.model.QuestionUseCaseModel

data class FormQuestionUiState(
    val questionType: QuestionType,
    val problem: String,
    val answerList: List<String>,
    val otherSelectionList: List<String>,
    val explanation: String,
    val problemImage: QuestionImage,
    val explanationImage: QuestionImage,
    val isAutoGenerateOtherSelections: Boolean,
    val isCheckAnswerOrder: Boolean
) {

    companion object {
        fun fromQuestionUseCaseModel(question: QuestionUseCaseModel) =
            FormQuestionUiState(
                questionType = question.type,
                problem = question.problem,
                answerList = question.answers,
                otherSelectionList = question.otherSelections,
                explanation = question.explanation,
                problemImage = question.problemImageUrl,
                explanationImage = question.explanationImageUrl,
                isAutoGenerateOtherSelections = question.isAutoGenerateOtherSelections,
                isCheckAnswerOrder = question.isCheckAnswerOrder
            )
    }

    // todo Unit テスト作成
    val shouldEnableCreateButton =
        when (questionType) {
            QuestionType.WRITE -> problem.isNotEmpty() && answerList.all { it.isNotEmpty() }
            QuestionType.SELECT -> problem.isNotEmpty() && answerList.all { it.isNotEmpty() } && otherSelectionList.all { it.isNotEmpty() }
            QuestionType.COMPLETE -> problem.isNotEmpty() && answerList.all { it.isNotEmpty() }
            QuestionType.SELECT_COMPLETE -> problem.isNotEmpty() && answerList.all { it.isNotEmpty() } && otherSelectionList.all { it.isNotEmpty() }
        }

    val shouldShowAnswerListCount =
        listOf(QuestionType.COMPLETE, QuestionType.SELECT_COMPLETE).contains(questionType)
    val shouldShowOtherSelectionList =
        listOf(QuestionType.SELECT, QuestionType.SELECT_COMPLETE).contains(questionType)
    val shouldShowOtherSelectionListCount =
        listOf(QuestionType.SELECT, QuestionType.SELECT_COMPLETE).contains(questionType)
    val shouldShowIsAutoGenerateOtherSelections =
        listOf(QuestionType.SELECT, QuestionType.SELECT_COMPLETE).contains(questionType)
    val shouldShowIsCheckAnswerOrder =
        listOf(QuestionType.COMPLETE, QuestionType.SELECT_COMPLETE).contains(questionType)
}
