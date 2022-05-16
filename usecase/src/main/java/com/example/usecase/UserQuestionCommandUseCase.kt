package com.example.usecase

import com.example.core.QuestionImage
import com.example.core.QuestionType
import com.example.domain.model.CreateQuestionRequest
import com.example.domain.model.QuestionId
import com.example.domain.model.WorkbookId
import com.example.domain.repository.WorkBookRepository
import com.example.usecase.model.QuestionUseCaseModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserQuestionCommandUseCase @Inject constructor(
    private val workBookRepository: WorkBookRepository
) {

    suspend fun createQuestion(
        workbookId: Long,
        type: Int,
        problem: String,
        answers: List<String>,
        explanation: String,
        problemImageUrl: String,
        explanationImageUrl: String,
        otherSelections: List<String>,
        isAutoGenerateOtherSelections: Boolean,
        isCheckAnswerOrder: Boolean
    ) =
        workBookRepository.createQuestion(
            workbookId = WorkbookId(workbookId),
            request = CreateQuestionRequest(
                questionType = QuestionType.valueOf(value = type),
                problem = problem,
                answers = answers,
                explanation = explanation,
                problemImageUrl = problemImageUrl,
                explanationImageUrl = explanationImageUrl,
                otherSelections = otherSelections,
                isAutoGenerateOtherSelections = isAutoGenerateOtherSelections,
                isCheckAnswerOrder = isCheckAnswerOrder
            )
        )

    suspend fun updateQuestion(question: QuestionUseCaseModel) =
        workBookRepository.updateQuestion(question.toQuestion())

    suspend fun updateQuestionContents(
        questionId: Long,
        type: QuestionType,
        problem: String,
        answers: List<String>,
        explanation: String,
        problemImageUrl: QuestionImage,
        explanationImageUrl: QuestionImage,
        otherSelections: List<String>,
        isAutoGenerateOtherSelections: Boolean,
        isCheckAnswerOrder: Boolean
    ) {
        val question = workBookRepository.getQuestion(QuestionId(questionId))
        workBookRepository.updateQuestion(
            question.copy(
                type = type,
                problem = problem,
                answers = answers,
                explanation = explanation,
                problemImageUrl = problemImageUrl,
                explanationImageUrl = explanationImageUrl,
                otherSelections = otherSelections,
                isAutoGenerateOtherSelections = isAutoGenerateOtherSelections,
                isCheckAnswerOrder = isCheckAnswerOrder
            )
        )
    }

    suspend fun deleteQuestions(workbookId: Long, questionIdList: List<Long>) {
        val workbook = workBookRepository.getWorkbook(WorkbookId(workbookId))
        val newQuestionList =
            workbook.questionList.filterNot { questionIdList.contains(it.id.value) }
        workBookRepository.updateWorkbook(workbook.copy(questionList = newQuestionList))
    }

    suspend fun swapQuestions(sourceQuestionId: Long, destQuestionId: Long) {
        val sourceQuestion =
            workBookRepository.getQuestion(questionId = QuestionId(sourceQuestionId))
        val destQuestion = workBookRepository.getQuestion(questionId = QuestionId(destQuestionId))
        workBookRepository.swapQuestion(sourceQuestion, destQuestion)
    }

    suspend fun moveQuestionsToOtherWorkbook(
        sourceWorkbookId: Long,
        destWorkbookId: Long,
        questionIdList: List<Long>
    ) {
        val sourceWorkbook = workBookRepository.getWorkbook(WorkbookId(sourceWorkbookId))
        val destWorkbook = workBookRepository.getWorkbook(WorkbookId(destWorkbookId))
        val questionList =
            sourceWorkbook.questionList.filter { questionIdList.contains(it.id.value) }

        workBookRepository.updateWorkbook(sourceWorkbook.copy(questionList = sourceWorkbook.questionList.filterNot {
            questionIdList.contains(it.id.value)
        }))
        workBookRepository.updateWorkbook(destWorkbook.copy(questionList = destWorkbook.questionList + questionList))
    }

    suspend fun copyQuestionsToOtherWorkbook(
        sourceWorkbookId: Long,
        destWorkbookId: Long,
        questionIdList: List<Long>
    ) {
        val sourceWorkbook = workBookRepository.getWorkbook(WorkbookId(sourceWorkbookId))
        val questionList =
            sourceWorkbook.questionList.filter { questionIdList.contains(it.id.value) }

        questionList.forEach {
            workBookRepository.createQuestion(
                workbookId = WorkbookId(value = destWorkbookId),
                request = CreateQuestionRequest.fromQuestion(it)
            )
        }
    }

    suspend fun copyQuestionInSameWorkbook(
        workbookId: Long,
        questionId: Long
    ) {
        val workbook = workBookRepository.getWorkbook(WorkbookId(workbookId))
        val question = workbook.questionList.find { it.id.value == questionId } ?: return

        workBookRepository.createQuestion(
            workbookId = workbook.id,
            request = CreateQuestionRequest.fromQuestion(question)
        )
    }
}
